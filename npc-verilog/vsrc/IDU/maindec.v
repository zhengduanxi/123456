`timescale 1ns / 1ps
module maindec(
    input       [31:0]      PC          ,
    input       [6:0]       op          ,
    output      [2:0]       ResultSrc   ,
    output                  MemWrite    ,
    output                  MemRead     ,
    output                  Branch      , 
    output                  ALUSrc      ,
    output                  RegWrite    , 
    output                  Jump        ,
    output      [2:0]       ImmSrc      ,
    output      [1:0]       ALUOp       ,
    output                  Ret
    );
    // reg define
    reg [14:0] controls;

    import "DPI-C" function void inv(input int PC);

    always@(*)begin
       case(op)
       // RegWrite_ImmSrc_ALUSrc_MemWrite_MemRead_ResultSrc_Branch_ALUOp_Jump_Ret
          7'b0000011: controls = 15'b1_000_1_0_1_001_0_00_0_0; // lw I-type R(rd) = Mr(src1 + imm, 4)
          7'b0100011: controls = 15'b0_001_1_1_0_000_0_00_0_0; // sw S-type Mw(src1 + imm, 4, src2)
          7'b0110011: controls = 15'b1_xxx_0_0_0_000_0_10_0_0; // add R-type R(rd) = src1 + src2
          7'b1100011: controls = 15'b0_010_0_0_0_000_1_01_0_0; // beq B-type s -> dnpc += src1 == src2 ? imm - 4: 0
          7'b0010011: controls = 15'b1_000_1_0_0_000_0_10_0_0; // addi I-type R(rd) = src1 + imm
          7'b0010111: controls = 15'b1_100_0_0_0_011_0_00_0_0; // auipc U-type R(rd) = s->pc + imm
          7'b0110111: controls = 15'b1_100_0_0_0_100_0_00_0_0; // lui U-type R(rd) = imm
          7'b1101111: controls = 15'b1_011_0_0_0_010_0_00_1_0; // jal J-type R(rd) = s->pc + 4; s->dnpc = s->pc + imm
          7'b1100111: controls = 15'b1_000_1_0_0_010_0_00_0_1; // jalr I-type R(rd) = s->pc + 4; s -> dnpc = (src1 + imm) & ~1
             default: begin inv(PC); controls = 15'bx_xxx_x_x_xxx_x_xx_x_x; end // ??? 
        endcase
    end
    assign {RegWrite, ImmSrc, ALUSrc, MemWrite, MemRead, ResultSrc, Branch, ALUOp, Jump, Ret} = controls;
    //RegWrite:有R(rd)为1,无R(rd)为0
    //ImmSrc:I-type为000,S-type为001,B-type为010,J-type为011,U-type为111
    //ALUSrc:src1和imm运算为1,和src2运算为0
    //MemWrite:向内存中写入为1
    //ResultSrc:取值=运算结果为000,从内存相应位置读出并写入到寄存器中为001,R(rd)=s->pc + 4为010,R(rd) = s -> pc + imm为011,R(rd) = imm为100
    //Branch:加上条件src1 == src2即Zero=1判断是否跳转
    //ALUOp:加法为00,减法为01,按funct3分为10
    //Jump:跳转s->dnpc = s->pc + imm为1
    //Ret:函数返回为R(1)
endmodule
