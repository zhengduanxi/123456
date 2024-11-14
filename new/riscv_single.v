`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2024/06/13 17:31:24
// Design Name: 
// Module Name: riscv_single
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


module riscv_single(
    input               clk,
    input               reset,
    input       [31:0]  Instr, 
    input       [31:0]  ReadData,
    output      [31:0]  PC,
    output      [31:0]  ALUResult,
    output      [31:0]  WriteData,
    output              MemWrite              
    );
    wire        ALUSrc, RegWrite, Jump, Zero;
    wire [1:0]  ResultSrc, ImmSrc;
    wire [2:0]  ALUControl;
    
controller controller_u(//??????? ????? ????? 000 ????? 00100 11
    .op                     (Instr[6:0])     ,//00100 11              
    .funct3                 (Instr[14:12])   ,//000
    .funct7b5               (Instr[30])      ,
    .Zero                   (Zero)           ,
    .ResultSrc              (ResultSrc)      ,
    .MemWrite               (MemWrite)       ,
    .PCSrc                  (PCSrc)          ,
    .ALUSrc                 (ALUSrc)         ,//ALU源选择信号
    .RegWrite               (RegWrite)       ,//寄存器写信号
    .Jump                   (Jump)           ,//跳转信号
    .ImmSrc                 (ImmSrc)         ,//立即数源选择信号
    .ALUControl             (ALUControl)     
    
    );
datapath datapath_u(
    .clk                    (clk)            ,                          
    .reset                  (reset)          ,
    .ResultSrc              (ResultSrc)      ,
    .PCSrc                  (PCSrc)          ,
    .ALUSrc                 (ALUSrc)         ,//ALU源选择信号
    .ALUControl             (ALUControl)     ,//控制ALU的操作
    .ImmSrc                 (ImmSrc)         ,
    .RegWrite               (RegWrite)       ,
    .Instr                  (Instr)          ,
    .ReadData               (ReadData)       ,
    .PC                     (PC)             ,
    .ALUResult              (ALUResult)      ,//算术逻辑单元（ALU）的结果
    .WriteData              (WriteData)      ,
    .Zero                   (Zero)           
    
    
    );
    
    
    
    
    
    
    
endmodule
