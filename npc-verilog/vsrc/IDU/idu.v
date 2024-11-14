`timescale 1ns / 1ps
module IDU(//??????? ????? ????? 000 ????? 00100 11
    input       [31:0]      PC          ,
    input       [31:0]      Instr       ,
    input                   Zero        ,
    output      [2:0]       ResultSrc   ,
    output                  MemWrite    ,
    output                  MemRead     ,
    output                  PCSrc       , 
    output                  ALUSrc      ,
    output                  RegWrite    , 
    output                  Jump        ,
    output                  Ret         ,
    output      [4:0]       ALUControl  ,
    output      [31:0]      ImmExt      ,
    output      [31:0]       len  
    );
    // wire define
     wire [1:0]     ALUOp ;
     wire [2:0]     ImmSrc;
     wire           Branch;
maindec md(
    .PC                  (PC)           ,
    .op                  (Instr[6:0])   ,     
    .ResultSrc           (ResultSrc)    ,
    .MemWrite            (MemWrite)     ,
    .MemRead             (MemRead)      ,
    .Branch              (Branch)       ,
    .ALUSrc              (ALUSrc)       ,
    .RegWrite            (RegWrite)     ,
    .Jump                (Jump)         ,
    .ImmSrc              (ImmSrc)       ,
    .ALUOp               (ALUOp)        ,
    .Ret                 (Ret)          
     );
aludec  ad(
    .op                  (Instr[6:0])   ,     
    .funct3              (Instr[14:12]) ,
    .funct7              (Instr[31:25]) ,
    .ALUOp               (ALUOp)        ,
    .ALUControl          (ALUControl)   ,
    .len                 (len)
     );
extend  ext(
    .instr          (Instr[31:7])   , 
    .immsrc         (ImmSrc)        ,
    .immext         (ImmExt)        
    );
     assign PCSrc = Branch & Zero | Jump;//PC跳转信号                      
endmodule
