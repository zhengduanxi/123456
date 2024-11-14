`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2024/06/13 16:45:07
// Design Name: 
// Module Name: riscv_top
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module riscv_top(
    input               clk,
    input               reset,
    output              MemWrite,//存储器写信号输出
    output      [31:0]  DataAdr,//数据地址输出
    output      [31:0]  WriteData//写入数据输出
               
    );
    wire  [31:0] PC, Instr, ReadData;
    riscv_single riscv_single_u(
     .clk                   (clk)       , 
     .reset                 (reset)     , 
     .PC                    (PC)        ,//存储程序计数器（PC）
     .Instr                 (Instr)     , 
     .MemWrite              (MemWrite)  ,
     .ALUResult             (DataAdr) ,  
     .WriteData             (WriteData) , 
     .ReadData              (ReadData)
    );
    imem imem_u(
     .A                     (PC)        ,
     .RD                    (Instr)//存储指令（Instr）
    );
    dmem dmem_u(
    . clk                   (clk)       , 
    . we                    (MemWrite)  , 
    . a                     (DataAdr)   , 
    . wd                    (WriteData) , 
    . rd                    (ReadData)//存储读出的数据 （ReadData）
    );
    
    
    
    
    
    
    
endmodule
