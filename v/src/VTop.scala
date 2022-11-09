package v

import chisel3._
import chisel3.util._

class RefModel(param: VParam) extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val req = Decoupled(new VReq(param))
    val resp = Flipped(Valid(new VResp(param)))
    val csrInterface = Flipped(Input(new LaneCsrInterface(param.laneParam.VLMaxWidth)))
    val storeBufferClear = Flipped(Input(Bool()))
    val tlPort = Flipped(Vec(param.tlBank, param.tlParam.bundle()))
  })
}

class VTop(param: VParam) extends Module {
  val v = Module(new V(param))
  val r = Module(new RefModel(param))

  r.io.clock := clock
  r.io.reset := reset

  v.req <> r.io.req
  v.resp <> r.io.resp
  v.csrInterface <> r.io.csrInterface
  v.storeBufferClear <> r.io.storeBufferClear
  v.tlPort <> r.io.tlPort
}