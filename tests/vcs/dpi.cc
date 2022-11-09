#include "dpi.h"
#include "vbridge_impl.h"

#include <stdio.h>

VBridgeImpl* ref = NULL;

extern "C" void refmodel_create(
  const char* binary, 
  uint64_t reset_vector) {
  ref = new VBridgeImpl();
  ref->setup(binary, "", reset_vector, 0);
  ref->run();
}

extern "C" void refmodel_tick(
 vector_inst_in_t* v_in, 
 tilelink_a_in_t* a0_in, 
 tilelink_a_in_t* a1_in, 
 tilelink_d_in_t* d0_in, 
 tilelink_d_in_t* d1_in, 
 vector_inst_out_t* v_out,
 tilelink_a_out_t* a0_out, 
 tilelink_a_out_t* a1_out,
 tilelink_d_out_t* d0_out, 
 tilelink_d_out_t* d1_out) {

  ref->top.req_ready = v_in->req_ready;
  ref->top.resp_valid = v_in->resp_valid;
  ref->top.resp_bits_data = v_in->resp_bits_data;
  ref->top.tlPort_0_a_valid = a0_in->valid;
  ref->top.tlPort_0_a_bits_opcode = a0_in->opcode;
  ref->top.tlPort_0_a_bits_param = a0_in->param;
  ref->top.tlPort_0_a_bits_size = a0_in->size;
  ref->top.tlPort_0_a_bits_source = a0_in->source;
  ref->top.tlPort_0_a_bits_address = a0_in->address;
  ref->top.tlPort_0_a_bits_mask = a0_in->mask;
  ref->top.tlPort_0_a_bits_data = a0_in->data;
  ref->top.tlPort_0_a_bits_corrupt = a0_in->corrupt;
  ref->top.tlPort_1_a_valid = a1_in->valid;
  ref->top.tlPort_1_a_bits_opcode = a1_in->opcode;
  ref->top.tlPort_1_a_bits_param = a1_in->param;
  ref->top.tlPort_1_a_bits_size = a1_in->size;
  ref->top.tlPort_1_a_bits_source = a1_in->source;
  ref->top.tlPort_1_a_bits_address = a1_in->address;
  ref->top.tlPort_1_a_bits_mask = a1_in->mask;
  ref->top.tlPort_1_a_bits_data = a1_in->data;
  ref->top.tlPort_1_a_bits_corrupt = a1_in->corrupt;
  ref->top.tlPort_0_d_ready = d0_in->ready;
  ref->top.tlPort_1_d_ready = d1_in->ready;

  ref->loop();

  v_out->req_valid = ref->top.req_valid;
  v_out->req_bits_inst = ref->top.req_bits_inst;
  v_out->req_bits_src1Data = ref->top.req_bits_src1Data;
  v_out->req_bits_src2Data = ref->top.req_bits_src2Data;
  v_out->csrInterface_vl = ref->top.csrInterface_vl;
  v_out->csrInterface_vStart = ref->top.csrInterface_vStart;
  v_out->csrInterface_vlmul = ref->top.csrInterface_vlmul;
  v_out->csrInterface_vSew = ref->top.csrInterface_vSew;
  v_out->csrInterface_vxrm = ref->top.csrInterface_vxrm;
  v_out->csrInterface_vta = ref->top.csrInterface_vta;
  v_out->csrInterface_vma = ref->top.csrInterface_vma;
  v_out->csrInterface_ignoreException = ref->top.csrInterface_ignoreException;
  v_out->storeBufferClear = ref->top.storeBufferClear;
  a0_out->ready = ref->top.tlPort_0_a_ready;
  a1_out->ready = ref->top.tlPort_1_a_ready;
  d0_out->valid = ref->top.tlPort_0_d_valid;
  d0_out->opcode = ref->top.tlPort_0_d_bits_opcode;
  d0_out->param = ref->top.tlPort_0_d_bits_param;
  d0_out->size = ref->top.tlPort_0_d_bits_size;
  d0_out->source = ref->top.tlPort_0_d_bits_source;
  d0_out->sink = ref->top.tlPort_0_d_bits_sink;
  d0_out->denied = ref->top.tlPort_0_d_bits_denied;
  d0_out->data = ref->top.tlPort_0_d_bits_data;
  d0_out->corrupt = ref->top.tlPort_0_d_bits_corrupt;
  d1_out->valid = ref->top.tlPort_1_d_valid;
  d1_out->opcode = ref->top.tlPort_1_d_bits_opcode;
  d1_out->param = ref->top.tlPort_1_d_bits_param;
  d1_out->size = ref->top.tlPort_1_d_bits_size;
  d1_out->source = ref->top.tlPort_1_d_bits_source;
  d1_out->sink = ref->top.tlPort_1_d_bits_sink;
  d1_out->denied = ref->top.tlPort_1_d_bits_denied;
  d1_out->data = ref->top.tlPort_1_d_bits_data;
  d1_out->corrupt = ref->top.tlPort_1_d_bits_corrupt;
}