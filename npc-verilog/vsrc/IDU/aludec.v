`timescale 1ns / 1ps
module aludec(
    input       [6:0]       op          ,
    input       [2:0]       funct3      ,
    input       [6:0]       funct7      ,
    input       [1:0]       ALUOp       ,
    output  reg      [4:0]  ALUControl  ,
    output  reg      [31:0]  len  
    );
    //wire define
    //wire   RtypeSub;
    //assign RtypeSub = funct7[5] & op[5];  // TRUE for R-type subtract 区别于srai
    always@(*)begin
       case(ALUOp)
           2'b00:begin ALUControl = 5'b00000; // addition
                    case(funct3)
                       3'b000:       len = 1; //sb
                       3'b001:       len = 2; // sh
                       3'b010:       len = 4; // sw lw
                       3'b100:       len = 1; // lbu
                       3'b101:       len = 2; // lhu
                       default:      len = 32'bxxx; // ???
                    endcase
           end
           2'b01:ALUControl = 5'b00001; // subtraction
           default: case(funct7)
             7'b0100000: case(funct3) //2'b10 R-type or I-type ALU
                       3'b000:       ALUControl = 5'b00001; // sub
                       3'b101:       ALUControl = op[5] ? 5'b10001 : 5'b10000; // sra //srai
                       3'b001:       ALUControl = 5'b01010;  //sll,slli
                       3'b010:       ALUControl = 5'b00010; // slt, slti
                       3'b011:       ALUControl = 5'b00011; // sltu, sltiu
                       3'b100:       ALUControl = 5'b00100; // xor, xori
                       3'b110:       ALUControl = 5'b00110; // or, ori
                       3'b111:       ALUControl = 5'b00111; // and, andi             
                       default:      ALUControl = 5'bxxxxx; // ???
                       endcase
             7'b0000001: case(funct3) //2'b10 R-type or I-type ALU
                       3'b000:       ALUControl = op[5] ? 5'b01000 : 5'b00000; // mul
                       3'b001:       ALUControl = op[5] ? 5'b01001 : 5'b00001;  //mulh
                       3'b010:       ALUControl = op[5] ? 5'bxxxx  : 5'b00010; // sll, slli
                       3'b011:       ALUControl = op[5] ? 5'b01011 : 5'b00011; // mulhu
                       3'b100:       ALUControl = op[5] ? 5'b01100 : 5'b00100; // div
                       3'b101:       ALUControl = op[5] ? 5'b01101 : 5'b00101; // divu
                       3'b110:       ALUControl = op[5] ? 5'b01110 : 5'b00110; // rem
                       3'b111:       ALUControl = op[5] ? 5'b01111 : 5'b00111; // remu
                       default:      ALUControl = 5'bxxxx; // ???
                       endcase
                default: case(funct3) //2'b10 R-type or I-type ALU
                       3'b000:begin
                                    //if (RtypeSub)
                                        //ALUControl = 5'b00001; // sub
                                    //else
                                        ALUControl = 5'b00000; // add, addi
                               end
                       3'b001:       ALUControl = 5'b01010;  //sll,slli
                       3'b010:       ALUControl = 5'b00010; // slt, slti
                       3'b011:       ALUControl = 5'b00011; // sltu, sltiu
                       3'b100:       ALUControl = 5'b00100; // xor, xori
                       3'b101:       ALUControl = 5'b00101; // srl,srli
                       3'b110:       ALUControl = 5'b00110; // or, ori
                       3'b111:       ALUControl = 5'b00111; // and, andi
                       default:      ALUControl = 5'bxxxxx; // ???
                       endcase
           endcase
       endcase
    end
endmodule
