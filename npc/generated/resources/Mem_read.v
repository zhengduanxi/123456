module Mem_read (
    input         ACLK    ,
    input         ARESETn ,
    input         ren     ,
    input  [31:0] raddr   ,
    output [31:0] rdata   ,
    input  [7:0]  wmask   ,
    output reg    rvalid  
);

  reg  [31:0] data;
  wire [31:0] addr;
  assign addr = {raddr[31:2], 2'b0};

import "DPI-C" function int paddr_read(
  input int addr,
  input int len
);

  reg [31:0] len;
  always @ (*) begin
    case(wmask)
     8'b00000001:  len = 1;
     8'b00000010:  len = 2;
     8'b00000100:  len = 4;
     default: len = 4;
    endcase
  end

  always @(posedge ACLK) begin
    if (ARESETn == 0) begin
    data <= 0;
    rvalid <= 0;
    end
    else if (ren) begin 
    data <= paddr_read(raddr, len);
    rvalid <= 1;
    end
    else begin
    data <= data;
    rvalid <= 0;
    end
  end

  assign rdata = data;

endmodule
