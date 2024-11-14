`timescale 1ns / 1ps
module maindec(
    input       [6:0]       op          ,
    output      [1:0]       ResultSrc   ,
    output                  MemWrite    ,
    output                  Branch      , 
    output                  ALUSrc      ,
    output                  RegWrite    , 
    output                  Jump        ,
    output      [1:0]       ImmSrc      ,
    output      [1:0]       ALUOp
    );
    // reg define
    reg [10:0] controls;

    always@(op)begin
       case(op)
       // RegWrite_ImmSrc_ALUSrc_MemWrite_ResultSrc_Branch_ALUOp_Jump
          7'b0000011: controls = 11'b1_00_1_0_01_0_00_0; // lw I-type
          7'b0100011: controls = 11'b0_01_1_1_00_0_00_0; // sw S-type
          7'b0110011: controls = 11'b1_xx_0_0_00_0_10_0; // add R-type
          7'b1100011: controls = 11'b0_10_0_0_00_1_01_0; // beq B-type
          7'b0010011: controls = 11'b1_00_1_0_00_0_10_0; // addi I-type
          7'b1101111: controls = 11'b1_11_0_0_10_0_00_1; // jal J-type
           default:      controls = 11'bx_xx_x_x_xx_x_xx_x; // ??? 
        endcase
    end
    assign {RegWrite, ImmSrc, ALUSrc, MemWrite,ResultSrc, Branch, ALUOp, Jump} = controls;

endmodule
