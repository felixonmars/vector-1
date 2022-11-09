#ifndef _DPI_H
#define _DPI_H

#include <cstdint>

typedef struct {
  char valid;
  char opcode;
  char param;
  char size;
  uint16_t source;
  uint32_t address;
  char mask;
  uint32_t data;
  char corrupt;
} tilelink_a_in_t;

typedef struct {
  char ready;
} tilelink_a_out_t;

typedef struct {
  char ready;
} tilelink_d_in_t;

typedef struct {
  char valid;
  char opcode;
  char param;
  char size;
  uint16_t source;
  uint16_t sink;
  char denied;
  uint32_t data;
  char corrupt;
} tilelink_d_out_t;

typedef struct {
  char req_ready;
  char resp_valid;
  uint32_t resp_bits_data;
} vector_inst_in_t;

typedef struct {
  char req_valid;
  uint32_t req_bits_inst;
  uint32_t req_bits_src1Data;
  uint32_t req_bits_src2Data;
  uint16_t csrInterface_vl;
  uint16_t csrInterface_vStart;
  char csrInterface_vlmul;
  char csrInterface_vSew;
  char csrInterface_vxrm;
  char csrInterface_vta;
  char csrInterface_vma;
  char csrInterface_ignoreException;
  char storeBufferClear;
} vector_inst_out_t;

#endif /* _DPI_H */
