module Ebreak (
  input wire [31:0] inst,
  input wire [31:0] pc,
  input wire [31:0] r10
);

import "DPI-C" function void ebreak();
import "DPI-C" function void npctrap(input int pc, input int r10);

  wire isEbreak = inst == 32'h00100073;

  always @(*) begin
      if (isEbreak) begin 
        npctrap(pc, r10);
        ebreak();
      end
  end
  
endmodule
