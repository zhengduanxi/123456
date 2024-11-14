`timescale 1ns / 1ps
module alu(
    input       [4:0]       ALUControl,
    input       [31:0]      SrcA,
    input       [31:0]      SrcB,
    input       [2:0]       funct3,
    output  reg             Zero,
    output  reg [31:0]      ALUResult
    );
    // wire define
  wire [31:0] condinvb, sum, intSrcA, intSrcB; 
  wire        cout, Zero1, Zero2, Zero3, Zero4, Zero5, Zero6;           // carry out of adder 
  reg  [63:0] tmp;

  assign condinvb = ALUControl[0] ? ~SrcB : SrcB; 
  assign {cout, sum} = SrcA + condinvb + {31'b0,ALUControl[0]};
  assign intSrcA = SrcA[31] ? (~SrcA + 1) : SrcA;
  assign intSrcB = SrcB[31] ? (~SrcB + 1) : SrcB;
  assign Zero1= (SrcA-SrcB==0)?1:0;//beq
  assign Zero2= (SrcA-SrcB!=0)?1:0;//bne
  assign Zero3= SrcA[31] ? ((SrcB[31]) ? (intSrcA > intSrcB) : 1) :((SrcB[31]) ? 0 : (SrcA < SrcB)); //(intSrcA<intSrcB)?1:0;//blt
  assign Zero4= SrcA[31] ? ((SrcB[31]) ? (intSrcA <= intSrcB) : 0) :((SrcB[31]) ? 1 : (SrcA >= SrcB)); //(intSrcA>=intSrcB)?1:0;//bge
  assign Zero5= (SrcA<SrcB)?1:0;//bltu
  assign Zero6= (SrcA>=SrcB)?1:0;//bgeu
  always@(*)begin 
    case (funct3) 
      3'b000:   Zero = Zero1;    // beq 
      3'b001:   Zero = Zero2;    // bne 
      3'b100:   Zero = Zero3;   // blt 
      3'b101:   Zero = Zero4;   // bge 
      3'b110:   Zero = Zero5;   // bltu 
      3'b111:   Zero = Zero6;   // bgeu 
      default:  Zero = 0; 
    endcase
  end 
   
  always@(*)begin 
    tmp = 0;
    case (ALUControl) 
      5'b00000:   ALUResult = sum;                    // add 
      5'b00001:   ALUResult = sum;                    // subtract 
      5'b00010:   ALUResult = {31'b0, intSrcA < intSrcB};// slt 
      5'b00011:   ALUResult = {31'b0, SrcA < SrcB};   // sltu 
      5'b00100:   ALUResult = SrcA ^ SrcB;            // xor 
      5'b00101:   ALUResult = SrcA >> SrcB;            // srl 
      5'b00110:   ALUResult = SrcA | SrcB;            // or 
      5'b00111:   ALUResult = SrcA & SrcB;            // and 

      5'b01000:   ALUResult = SrcA*SrcB;              // mul 
      5'b01001:   begin tmp = {{32{intSrcA[31]}}, intSrcA} * {{32{intSrcB[31]}}, intSrcB};
                  ALUResult = tmp[63:32]; end         // mulh 
      5'b01010:   ALUResult = SrcA << SrcB;           //sll,slli 
      5'b01011:   begin tmp = {{32{SrcA[31]}}, SrcA} * {{32{SrcB[31]}}, SrcB};
                  ALUResult = tmp[63:32]; end          // mulhu 
      5'b01100:   ALUResult = intSrcA ^ intSrcB;       // div 
      5'b01101:   ALUResult = SrcA / SrcB;            // divu 
      5'b01110:   ALUResult = intSrcA % intSrcB;      // rem 
      5'b01111:   ALUResult = SrcA % SrcB;            // remu 
      5'b10000:   ALUResult = ({32{SrcA[31]}} << (32 - SrcB[4:0])) | (SrcA >> SrcB[4:0]); // srai 
      5'b10001:   ALUResult = ({32{SrcA[31]}} << (32 - SrcB)) | (SrcA >> SrcB); // sra 
      default:  ALUResult = 32'bx; 
    endcase
  end 
endmodule
