#ifndef _VV_H
#define _VV_H

#include <cstdint>

using CData = uint8_t;    ///< Data representing 'bit' of 1-8 packed bits
using SData = uint16_t;   ///< Data representing 'bit' of 9-16 packed bits
using IData = uint32_t;   ///< Data representing 'bit' of 17-32 packed bits
using QData = uint64_t;   ///< Data representing 'bit' of 33-64 packed bits
using EData = uint32_t;   ///< Data representing one element of WData array
using WData = EData;      ///< Data representing >64 packed bits (used as pointer)

#define VL_SIG8(name, msb, lsb) CData name  ///< Declare signal, 1-8 bits
#define VL_SIG16(name, msb, lsb) SData name  ///< Declare signal, 9-16 bits
#define VL_SIG64(name, msb, lsb) QData name  ///< Declare signal, 33-64 bits
#define VL_SIG(name, msb, lsb) IData name  ///< Declare signal, 17-32 bits
#define VL_IN8(name, msb, lsb) CData name  ///< Declare input signal, 1-8 bits
#define VL_IN16(name, msb, lsb) SData name  ///< Declare input signal, 9-16 bits
#define VL_IN64(name, msb, lsb) QData name  ///< Declare input signal, 33-64 bits
#define VL_IN(name, msb, lsb) IData name  ///< Declare input signal, 17-32 bits
#define VL_INOUT8(name, msb, lsb) CData name  ///< Declare bidir signal, 1-8 bits
#define VL_INOUT16(name, msb, lsb) SData name  ///< Declare bidir signal, 9-16 bits
#define VL_INOUT64(name, msb, lsb) QData name  ///< Declare bidir signal, 33-64 bits
#define VL_INOUT(name, msb, lsb) IData name  ///< Declare bidir signal, 17-32 bits
#define VL_OUT8(name, msb, lsb) CData name  ///< Declare output signal, 1-8 bits
#define VL_OUT16(name, msb, lsb) SData name  ///< Declare output signal, 9-16 bits
#define VL_OUT64(name, msb, lsb) QData name  ///< Declare output signal, 33-64bits
#define VL_OUT(name, msb, lsb) IData name  ///< Declare output signal, 17-32 bits

class Verilated {
public:
  static void traceEverOn(bool flag) {};
};

class VerilatedContext {
public:
    void commandArgs(int argc, char **argv) {};
    int time(int a) {return a;};
    int time() {return 0;};
    void timeInc(int a) {};
};

class VerilatedFstC {
public:
    void dump(int a) {};
    void open(const char *a) {};
    bool isOpen() {return false;};
    void close() {};
};

class VV {
public:
  VL_IN8(clock,0,0);
  VL_IN8(reset,0,0);
  VL_IN(req_bits_inst,31,0);
  VL_IN(req_bits_src1Data,31,0);
  VL_IN16(csrInterface_vStart,9,0);
  VL_IN8(tlPort_0_d_bits_opcode,2,0);
  VL_IN8(tlPort_1_d_bits_opcode,2,0);
  VL_IN8(tlPort_1_d_bits_size,1,0);
  VL_IN16(tlPort_1_d_bits_source,9,0);
  VL_IN16(tlPort_1_d_bits_sink,9,0);
  VL_IN16(tlPort_0_d_bits_sink,9,0);
  VL_IN8(tlPort_0_d_valid,0,0);
  VL_IN8(csrInterface_vxrm,1,0);
  VL_IN8(req_valid,0,0);
  VL_OUT8(req_ready,0,0);
  VL_OUT8(resp_valid,0,0);
  VL_IN8(tlPort_1_d_valid,0,0);
  VL_IN8(tlPort_1_d_bits_denied,0,0);
  VL_IN8(tlPort_1_d_bits_corrupt,0,0);
  VL_OUT8(tlPort_0_d_ready,0,0);
  VL_OUT8(tlPort_1_d_ready,0,0);
  VL_IN(tlPort_1_d_bits_data,31,0);
  VL_IN8(tlPort_0_d_bits_param,2,0);
  VL_IN8(tlPort_0_d_bits_size,1,0);
  VL_IN8(tlPort_0_d_bits_denied,0,0);
  VL_IN8(tlPort_0_d_bits_corrupt,0,0);
  VL_IN8(tlPort_1_d_bits_param,2,0);
  VL_IN16(tlPort_0_d_bits_source,9,0);
  VL_IN(tlPort_0_d_bits_data,31,0);
  VL_IN8(tlPort_0_a_ready,0,0);
  VL_IN8(tlPort_1_a_ready,0,0);
  VL_IN8(csrInterface_vlmul,2,0);
  VL_OUT8(tlPort_0_a_bits_size,1,0);
  VL_OUT8(tlPort_1_a_bits_size,1,0);
  VL_OUT(tlPort_0_a_bits_data,31,0);
  VL_OUT(tlPort_1_a_bits_data,31,0);
  VL_IN(req_bits_src2Data,31,0);
  VL_OUT8(tlPort_0_a_valid,0,0);
  VL_OUT8(tlPort_0_a_bits_opcode,2,0);
  VL_OUT8(tlPort_0_a_bits_mask,3,0);
  VL_OUT8(tlPort_1_a_valid,0,0);
  VL_OUT8(tlPort_1_a_bits_opcode,2,0);
  VL_OUT8(tlPort_1_a_bits_mask,3,0);
  VL_OUT16(tlPort_0_a_bits_source,9,0);
  VL_OUT16(tlPort_1_a_bits_source,9,0);
  VL_OUT(tlPort_0_a_bits_address,31,0);
  VL_OUT(tlPort_1_a_bits_address,31,0);
  VL_IN16(csrInterface_vl,9,0);
  VL_IN8(csrInterface_vSew,1,0);
  VL_IN8(csrInterface_vta,0,0);
  VL_IN8(csrInterface_vma,0,0);
  VL_IN8(csrInterface_ignoreException,0,0);
  VL_IN8(storeBufferClear,0,0);
  VL_OUT8(tlPort_0_a_bits_param,2,0);
  VL_OUT8(tlPort_0_a_bits_corrupt,0,0);
  VL_OUT8(tlPort_1_a_bits_param,2,0);
  VL_OUT8(tlPort_1_a_bits_corrupt,0,0);
  VL_OUT(resp_bits_data,31,0);

  VV();
  void eval() {};
  void dump() {};
  void final() {};
  void trace(VerilatedFstC *tfp, int levels) {};

};

#endif /* _VV_H */
