#pragma once

#ifdef __VCS
#include "vpi_user.h"
#else
#include <verilated_vpi.h>
#endif

inline auto vpi_get_integer(const char *name) {
  vpiHandle handle = vpi_handle_by_name((PLI_BYTE8 *) name, nullptr);
  s_vpi_value val;
  val.format = vpiIntVal;
  vpi_get_value(handle, &val);
  return val.value.integer;
}
