module Mem_write (
    input         ACLK    ,
    input         ARESETn ,
    input         wen     ,
    input  [31:0] waddr   ,
    input  [31:0] wdata   ,
    input  [7:0]  wmask   ,
    output reg    wdone   
);

import "DPI-C" function void paddr_write(
  input int  addr,
  input int  len,
  input int  data
);

reg [31:0] len;
always @ (*) begin
  case(wmask)
     8'b00000001:  len = 1;
     8'b00000010:  len = 1;
     8'b00000100:  len = 1;
     8'b00001000:  len = 1;
     8'b00000011:  len = 2;
     8'b00001100:  len = 2;
     8'b00001111:  len = 4;
     default: len = 0;
  endcase
end

  always @(posedge ACLK) begin
    if (ARESETn == 0) wdone <= 0;
    else if (wen) begin
    paddr_write(waddr, len, wdata);
    wdone <= 1;
    end
    else wdone <= 0;
  end

endmodule
