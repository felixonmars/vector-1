module TestBench;

  reg clock = 1'b0;
  reg reset = 1'b1;
  reg [63:0] cycle_count = 0;
  reg [63:0] max_cycles = 0;
  string wave = "";

  initial begin
    $value$plusargs("cycles=%d", max_cycles);
    $value$plusargs("wave=%s", wave);
    $fsdbDumpfile({wave, ".fsdb"});
    $fsdbDumpon;
    $fsdbDumpvars(0, "+all");
  end

  always #(0.5) clock = ~clock;
  initial #(10.1) reset = 0;

  always @(posedge clock) begin
    cycle_count = cycle_count + 1;
    if (max_cycles > 0 && cycle_count > max_cycles) begin
      $display("[*] timeout after %d", max_cycles);
      $fsdbDumpoff;
      $finish();
    end
  end

  VTop dut(
    .clock(clock),
    .reset(reset));
  
endmodule