`timescale 1ns / 1ps
module datapath(
    input               clk         ,
    input               reset       ,
    input   [1:0]       ResultSrc   ,
    input               PCSrc       ,
    input               ALUSrc      ,
    input   [2:0]       ALUControl  ,
    input   [1:0]       ImmSrc      ,
    input               RegWrite    ,
    input   [31:0]      Instr       ,
    input   [31:0]      ReadData    ,
    output  [31:0]      PC          ,
    output  [31:0]      ALUResult   ,
    output  [31:0]      WriteData   ,
    output              Zero
    );
 
 // wire define
    wire   [31:0] PCNext, PCPlus4, PCTarget;
    wire   [31:0] ImmExt;
    wire   [31:0] SrcA, SrcB;
    wire   [31:0] Result;
 
 
    
// next PC logic  ��һ���鱾�ϵ�д�����Ҹо��ǳ����ȡ�
// ��Ȼ�Ѵ��������ӷ���ȫ����ģ�黯����ģ�黯Ӧ�õ��˼��£�
//����Ұ��Լ���˼·д�����ģ��Ͳ����и�ϸ�ķֿ��ˡ�
    REG#(32,0) pcreg(
    .clk               (clk)    ,
    .rst               (reset)  ,
    .din               (PCNext) ,
    .dout              (PC)     ,
    .wen               (1)
    );
    adder#(32) pcadd4(
    .a              (PC)        ,
    .b              (32'd4)     ,
    .y              (PCPlus4)
    );
    adder#(32) pcaddbranch(
    .a              (PC)        ,
    .b              (ImmExt)    ,
    .y              (PCTarget)
    );
    mux2#(32)  pcmux(
    .d0             (PCPlus4)   ,
    .d1             (PCTarget)  ,
    .s              (PCSrc)     ,
    .y              (PCNext)    
    );
//  register file logic
    RegisterFile#(5,32) rf(
    .clk            (clk)           ,
    .A1             (Instr[19:15])  ,
    .A2             (Instr[24:20])  ,
    .waddr          (Instr[11:7])   ,
    .wdata          (Result)        ,
    .wen            (RegWrite)      ,
    .RD1            (SrcA)          ,
    .RD2            (WriteData)        
    );
    extend  ext(
    .instr          (Instr[31:7])   , 
    .immsrc         (ImmSrc)        ,
    .immext         (ImmExt)        
    );
    //ALU logic
     mux2 #(32)   srcbmux(
     .d0            (WriteData)     , 
     .d1            (ImmExt)        , 
     .s             (ALUSrc)        , 
     .y             (SrcB)
     );
     alu alu_u(
     .ALUControl    (ALUControl)    ,
     .SrcA          (SrcA)          ,      
     .SrcB          (SrcB)          ,      
     .Zero          (Zero)          ,      
     .ALUResult     (ALUResult)
     );
     mux3#(32) resultmux(
     .d0            (ALUResult)     ,            
     .d1            (ReadData)      ,
     .d2            (PCPlus4)       ,
     .s             (ResultSrc)     ,
     .y             (Result)        
     );
    
endmodule
