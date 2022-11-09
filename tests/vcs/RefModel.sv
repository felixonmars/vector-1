typedef struct {
  byte valid;
  byte opcode;
  byte param;
  byte size;
  shortint unsigned source;
  int unsigned address;
  byte mask;
  int unsigned data;
  byte corrupt;
} tilelink_a_in_t;

typedef struct {
  byte ready;
} tilelink_a_out_t;

typedef struct {
  byte ready;
} tilelink_d_in_t;

typedef struct {
  byte valid;
  byte opcode;
  byte param;
  byte size;
  shortint unsigned source;
  shortint unsigned sink;
  byte denied;
  int unsigned data;
  byte corrupt;
} tilelink_d_out_t;

typedef struct {
  byte req_ready;
  byte resp_valid;
  int unsigned resp_bits_data;
} vector_inst_in_t;

typedef struct {
  byte req_valid;
  int unsigned req_bits_inst;
  int unsigned req_bits_src1Data;
  int unsigned req_bits_src2Data;
  shortint unsigned csrInterface_vl;
  shortint unsigned csrInterface_vStart;
  byte csrInterface_vlmul;
  byte csrInterface_vSew;
  byte csrInterface_vxrm;
  byte csrInterface_vta;
  byte csrInterface_vma;
  byte csrInterface_ignoreException;
  byte storeBufferClear;
} vector_inst_out_t;

import "DPI-C" function int refmodel_create(
  string filename, 
  longint unsigned reset_vector);

import "DPI-C" function void refmodel_tick(
  input vector_inst_in_t v_in, 
  input tilelink_a_in_t a0_in, 
  input tilelink_a_in_t a1_in, 
  input tilelink_d_in_t d0_in, 
  input tilelink_d_in_t d1_in, 
  output vector_inst_out_t v_out,
  output tilelink_a_out_t a0_out, 
  output tilelink_a_out_t a1_out,
  output tilelink_d_out_t d0_out, 
  output tilelink_d_out_t d1_out);

module RefModel(
  input         clock,
                reset,
  output        req_valid,
  output [31:0] req_bits_inst,
                req_bits_src1Data,
                req_bits_src2Data,
  output [9:0]  csrInterface_vl,
                csrInterface_vStart,
  output [2:0]  csrInterface_vlmul,
  output [1:0]  csrInterface_vSew,
                csrInterface_vxrm,
  output        tlPort_0_d_valid,
  output [2:0]  tlPort_0_d_bits_opcode,
                tlPort_0_d_bits_param,
  output [1:0]  tlPort_0_d_bits_size,
  output [9:0]  tlPort_0_d_bits_source,
                tlPort_0_d_bits_sink,
  output        tlPort_0_d_bits_denied,
  output [31:0] tlPort_0_d_bits_data,
  output        tlPort_0_d_bits_corrupt,
                tlPort_0_a_ready,
                tlPort_1_d_valid,
  output [2:0]  tlPort_1_d_bits_opcode,
                tlPort_1_d_bits_param,
  output [1:0]  tlPort_1_d_bits_size,
  output [9:0]  tlPort_1_d_bits_source,
                tlPort_1_d_bits_sink,
  output        tlPort_1_d_bits_denied,
  output [31:0] tlPort_1_d_bits_data,
  output        tlPort_1_d_bits_corrupt,
                tlPort_1_a_ready,
  input [31:0]  resp_bits_data,
  input         req_ready,
                resp_valid,
                tlPort_0_d_ready,
                tlPort_0_a_valid,
  input  [2:0]  tlPort_0_a_bits_opcode,
                tlPort_0_a_bits_param,
  input  [1:0]  tlPort_0_a_bits_size,
  input  [9:0]  tlPort_0_a_bits_source,
  input  [31:0] tlPort_0_a_bits_address,
  input  [3:0]  tlPort_0_a_bits_mask,
  input  [31:0] tlPort_0_a_bits_data,
  input         tlPort_0_a_bits_corrupt,
                tlPort_1_d_ready,
                tlPort_1_a_valid,
  input  [2:0]  tlPort_1_a_bits_opcode,
                tlPort_1_a_bits_param,
  input  [1:0]  tlPort_1_a_bits_size,
  input  [9:0]  tlPort_1_a_bits_source,
  input  [31:0] tlPort_1_a_bits_address,
  input  [3:0]  tlPort_1_a_bits_mask,
  input  [31:0] tlPort_1_a_bits_data,
  input         tlPort_1_a_bits_corrupt,
  
  output        csrInterface_vta,
                csrInterface_vma,
                csrInterface_ignoreException,
                storeBufferClear);

  string binary = "";
  reg [63:0] reset_vector = 'h1000;
  tilelink_a_in_t a0_in, a1_in;
  tilelink_a_out_t a0_out, a1_out;
  tilelink_d_in_t d0_in, d1_in;
  tilelink_d_out_t d0_out, d1_out;
  vector_inst_in_t v_in;
  vector_inst_out_t v_out;

  initial begin
    $value$plusargs("bin=%s", binary);
    assert(binary != "") else $fatal("Testcase is required!");
    $value$plusargs("reset_vector=%d", reset_vector);
    refmodel_create(binary, reset_vector);
  end

  /* data */
  always @(posedge clock) begin
    v_in.req_ready = req_ready;
    v_in.resp_valid = resp_valid;
    v_in.resp_bits_data = resp_bits_data;

    a0_in.valid = tlPort_0_a_valid;
    a0_in.opcode = tlPort_0_a_bits_opcode;
    a0_in.param = tlPort_0_a_bits_param;
    a0_in.size = tlPort_0_a_bits_size;
    a0_in.source = tlPort_0_a_bits_source;
    a0_in.address = tlPort_0_a_bits_address;
    a0_in.mask = tlPort_0_a_bits_mask;
    a0_in.data = tlPort_0_a_bits_data;
    a0_in.corrupt = tlPort_0_a_bits_corrupt;

    a1_in.valid = tlPort_1_a_valid;
    a1_in.opcode = tlPort_1_a_bits_opcode;
    a1_in.param = tlPort_1_a_bits_param;
    a1_in.size = tlPort_1_a_bits_size;
    a1_in.source = tlPort_1_a_bits_source;
    a1_in.address = tlPort_1_a_bits_address;
    a1_in.mask = tlPort_1_a_bits_mask;
    a1_in.data = tlPort_1_a_bits_data;
    a1_in.corrupt = tlPort_1_a_bits_corrupt;

    d0_in.ready = tlPort_0_d_ready;

    d1_in.ready = tlPort_1_d_ready;

    if (!reset) begin
      refmodel_tick(
        v_in,
        a0_in,
        a1_in,
        d0_in,
        d1_in,
        v_out,
        a0_out,
        a1_out,
        d0_out,
        d1_out);
    end

  end

  assign tlPort_0_d_valid = d0_out.valid && !reset;
  assign tlPort_0_d_bits_opcode = d0_out.opcode;
  assign tlPort_0_d_bits_param = d0_out.param;
  assign tlPort_0_d_bits_size = d0_out.size;
  assign tlPort_0_d_bits_source = d0_out.source;
  assign tlPort_0_d_bits_sink = d0_out.sink;
  assign tlPort_0_d_bits_denied = d0_out.denied;
  assign tlPort_0_d_bits_data = d0_out.data;
  assign tlPort_0_d_bits_corrupt = d0_out.corrupt;

  assign tlPort_1_d_valid = d1_out.valid && !reset;
  assign tlPort_1_d_bits_opcode = d1_out.opcode;
  assign tlPort_1_d_bits_param = d1_out.param;
  assign tlPort_1_d_bits_size = d1_out.size;
  assign tlPort_1_d_bits_source = d1_out.source;
  assign tlPort_1_d_bits_sink = d1_out.sink;
  assign tlPort_1_d_bits_denied = d1_out.denied;
  assign tlPort_1_d_bits_data = d1_out.data;
  assign tlPort_1_d_bits_corrupt = d1_out.corrupt;

  assign req_valid = v_out.req_valid && !reset;
  assign req_bits_inst = v_out.req_bits_inst;
  assign req_bits_src1Data = v_out.req_bits_src1Data;
  assign req_bits_src2Data = v_out.req_bits_src2Data;
  assign csrInterface_vl = v_out.csrInterface_vl;
  assign csrInterface_vStart = v_out.csrInterface_vStart;
  assign csrInterface_vlmul = v_out.csrInterface_vlmul;
  assign csrInterface_vSew = v_out.csrInterface_vSew;
  assign csrInterface_vxrm = v_out.csrInterface_vxrm;
  assign csrInterface_vta = v_out.csrInterface_vta;
  assign csrInterface_vma = v_out.csrInterface_vma;
  assign csrInterface_ignoreException = v_out.csrInterface_ignoreException;
  assign storeBufferClear = v_out.storeBufferClear;

  assign tlPort_0_a_ready = a0_out.ready && !reset;
  assign tlPort_1_a_ready = a1_out.ready && !reset;

endmodule