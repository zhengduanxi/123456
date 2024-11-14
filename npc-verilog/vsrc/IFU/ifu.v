`timescale 1ns / 1ps
module IFU              (
    input               clk         ,
    input               reset       ,
    input               PCSrc       ,
    input   [31:0]      ImmExt      ,
    input               Ret         ,
    input   [31:0]      ALUResult   ,
    output  reg [31:0]  PC          ,
    output  [31:0]      PCPlus4     ,
    output  [31:0]      PCTarget    ,
    output  [31:0]      PCNext    
);
    initial begin PC = 32'h80000000; end
    wire   [31:0] PCRet, PCMux;
    Reg#(32, 32'h80000000) pcreg(
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
    assign PCRet = ALUResult & 32'hFFFFFFFE;
    mux2#(32)  pcmux1(
    .d0             (PCPlus4)   ,
    .d1             (PCRet)     ,
    .s              (Ret)       ,
    .y              (PCMux)    
    );
    adder#(32) pcaddbranch(//跳转
    .a              (PC)        ,
    .b              (ImmExt)    ,
    .y              (PCTarget)
    );
    mux2#(32)  pcmux2(
    .d0             (PCMux)     ,
    .d1             (PCTarget)  ,
    .s              (PCSrc)     ,
    .y              (PCNext)    
    );
endmodule
