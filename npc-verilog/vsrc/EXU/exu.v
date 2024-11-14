module EXU(
    input               clk,
    input   [31:0]      Instr       ,
    input               RegWrite    ,
    input   [31:0]      ImmExt      ,
    input               ALUSrc      ,
    input   [4:0]       ALUControl  ,
    input   [31:0]      ReadData    ,
    input   [31:0]      PCPlus4     ,
    input   [31:0]      PCTarget    ,
    input   [2:0]       ResultSrc   ,
    output  [31:0]      WriteData   ,
    output              Zero        ,
    output  [31:0]      ALUResult   ,
    output  [31:0]      R10
    );
    // wire define
    wire   [31:0] SrcA, SrcB;
    wire   [31:0] Result;
RegisterFile#(5,32) rf(
    .clk            (clk)           ,
    .A1             (Instr[19:15])  ,
    .A2             (Instr[24:20])  ,
    .waddr          (Instr[11:7])   ,
    .wdata          (Result)        ,
    .wen            (RegWrite)      ,
    .RD1            (SrcA)          ,
    .RD2            (WriteData)     ,
    .R10            (R10)
    );     
mux2#(32)    srcbmux(
     .d0            (WriteData)     , 
     .d1            (ImmExt)        , 
     .s             (ALUSrc)        , 
     .y             (SrcB)
     );
alu alu_u(
     .ALUControl    (ALUControl)    ,
     .SrcA          (SrcA)          ,      
     .SrcB          (SrcB)          ,
     .funct3        (Instr[14:12])  ,      
     .Zero          (Zero)          ,      
     .ALUResult     (ALUResult)
     );
mux5#(32) resultmux(
     .d0            (ALUResult)     ,            
     .d1            (ReadData)      ,
     .d2            (PCPlus4)       ,
     .d3            (PCTarget)      ,
     .d4            (ImmExt)        ,
     .s             (ResultSrc)     ,
     .y             (Result)        
     );
endmodule
