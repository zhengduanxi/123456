`timescale 1ns / 1ps
module dmem(
    input                   clk  , 
    input                   we   ,
    input                   valid,
    input       [31:0]      a    , 
    input       [31:0]      wd   ,
    input       [31:0]       len  ,
    output      [31:0]      rd
    );
    //reg define

//mtrace    
import "DPI-C" function int paddr_read(input int raddr, input int len);
import "DPI-C" function void paddr_write(input int waddr, input int len, input int wdata);
import "DPI-C" function void display_memory_read(input int addr, input int len, input int data);
import "DPI-C" function void display_memory_write(input int addr, int len, input int data);
/*reg [31:0] rdata;
always @(*) begin
    if (valid) rdata = pmem_read(a, 4);
end
always @(posedge clk) begin 
    if (we) pmem_write(a, 4, wd); // 有写请求时
end*/

reg [31:0] rdata;
always @(*) begin
  if (valid || we) begin // 有读写请求时
    rdata = paddr_read(a, len);
    display_memory_read(a, len, rdata);
    if (we) begin // 有写请求时
      paddr_write(a, len, wd);
      display_memory_write(a, len, wd);
    end
  end
  else begin
    rdata = 0;
  end
end
assign rd = rdata;
    /*reg [31:0] RAM[63:0];
    assign rd = RAM[a]; // word aligned
    always @(posedge clk)
        if (we) RAM[a] <= wd;*/
 endmodule
