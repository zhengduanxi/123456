`timescale 1ns / 1ps
module RegisterFile #(ADDR_WIDTH = 5, DATA_WIDTH = 32) (
  input clk,
  input [ADDR_WIDTH-1:0] A1  ,
  input [ADDR_WIDTH-1:0] A2  ,
  input [ADDR_WIDTH-1:0] waddr,
  input [DATA_WIDTH-1:0] wdata,
  input wen,
  output [DATA_WIDTH-1:0] RD1 ,
  output [DATA_WIDTH-1:0] RD2,
);
  reg [DATA_WIDTH-1:0] rf [2**ADDR_WIDTH-1:0];
  always @(posedge clk) begin
    if (wen) rf[waddr] <= wdata;
  end
  assign RD1 = (A1 != 0) ? rf[A1] : 0;
  assign RD2 = (A2 != 0) ? rf[A2] : 0;
endmodule

