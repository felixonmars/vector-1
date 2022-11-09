init:
	git submodule update --init

patch:
	find patches -type f | awk -F/ '{print("(echo "$$0" && cd dependencies/" $$2 " && git apply -3 --ignore-space-change --ignore-whitespace ../../" $$0 ")")}' | sh

depatch:
	git submodule update
	git submodule foreach git restore -S -W .
	git submodule foreach git clean -xdf

compile:
	mill -i vector.compile

bump:
	git submodule foreach git stash
	git submodule update --remote
	git add dependencies

bsp:
	mill -i mill.bsp.BSP/install

update-patches:
	rm -rf patches
	sed '/BEGIN-PATCH/,/END-PATCH/!d;//d' readme.md | awk '{print("mkdir -p patches/" $$1 " && wget " $$2 " -P patches/" $$1 )}' | parallel
	git add patches

clean:
	git clean -fd

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

test:
	mill -i tests.smoketest.run --cycles 100

HOME_DIR	:= $(shell pwd)
OUT_DIR 	:= $(HOME_DIR)/out
VCS_RUN_DIR	:= $(OUT_DIR)/vcs
VCS_BLD_DIR := $(VCS_RUN_DIR)/build

TB_SRC_DIR	:= $(HOME_DIR)/tests/vcs
EMU_SRC_DIR	:= $(HOME_DIR)/tests/emulator/src
VLG_BLD_DIR	:= $(OUT_DIR)/tests/elaborate/elaborate.dest
REF_SRC_DIR	:= $(HOME_DIR)/dependencies/riscv-isa-sim
REF_BLD_DIR	:= $(OUT_DIR)/tests/emulator/spike/compile.dest

TOP			:= TestBench
CXX_FLAGS	:= -std=c++17 -DFMT_HEADER_ONLY -D__VCS \
			   -I$(EMU_SRC_DIR) -I$(REF_SRC_DIR)/riscv -I$(REF_SRC_DIR)/softfloat -I$(REF_SRC_DIR)/fesvr -I$(REF_BLD_DIR) -I$(TB_SRC_DIR)
LD_FLAGS	:= -L$(REF_BLD_DIR) -Wl,-rpath,$(REF_BLD_DIR) -lriscv -lfmt -lglog
VCS_FLAGS	:= -full64 -sverilog +v2k -debug_acc+all -timescale=1ns/10ps +lint=TFIPC-L \
			   +incdir+$(VLG_BLD_DIR)+$(TB_SRC_DIR) -top $(TOP) -F $(VLG_BLD_DIR)/filelist.f
SIMV_FLAGS	:= +cycles=200 +bin=$(OUT_DIR)/tests/cases/smoketest/compile.dest/smoketest +wave=wave

VLG_SRC		:= $(TB_SRC_DIR)/RefModel.sv $(TB_SRC_DIR)/VTop.sv $(TB_SRC_DIR)/TestBench.sv
CXX_SRC		:= $(TB_SRC_DIR)/dpi.cc $(TB_SRC_DIR)/VV.cc $(EMU_SRC_DIR)/vbridge_impl.cc $(EMU_SRC_DIR)/spike_event.cc

vcs:
	rm -rf $(VCS_BLD_DIR); mkdir -p $(VCS_BLD_DIR)
	cd $(VCS_BLD_DIR); \
	vcs $(VCS_FLAGS) $(VCS_FLAGS) -CFLAGS "$(CXX_FLAGS)" -LDFLAGS "$(LD_FLAGS)" $(VLG_SRC) $(CXX_SRC) && \
	cd $(VCS_RUN_DIR); \
	$(VCS_BLD_DIR)/simv $(SIMV_FLAGS)


verdi:
	verdi $(VCS_FLAGS) $(VCS_FLAGS) -CFLAGS "$(CXX_FLAGS)" -LDFLAGS "$(LD_FLAGS)" $(VLG_SRC) $(CXX_SRC) -ssf $(VCS_RUN_DIR)/wave.fsdb -sswr signal.rc 
