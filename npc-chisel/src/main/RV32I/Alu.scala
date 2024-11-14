import chisel3._
import Control._
import chisel3.util.MuxLookup

class AluIo(dataWidth: Int, addrWidth: Int) extends Bundle {
  val imm      = Input(UInt(dataWidth.W))
  val rs1_data = Input(UInt(dataWidth.W))
  val rs2_data = Input(UInt(dataWidth.W))
  val pc_out   = Input(UInt(addrWidth.W))
  val Alu_op   = Input(UInt(4.W))
  val A_sel    = Input(UInt(1.W))
  val B_sel    = Input(UInt(1.W))
  val Alu_out  = Output(UInt(dataWidth.W))
}

class Alu(val dataWidth: Int, val addrWidth: Int) extends Module {
  val io = IO(new AluIo(dataWidth, addrWidth))

  val A = Wire(UInt(dataWidth.W))
  val B = Wire(UInt(dataWidth.W))
  val shamt = Wire(UInt(5.W))

  A := MuxLookup(io.A_sel, A_SEL_XXX)(Seq(A_SEL_RS1->io.rs1_data, A_SEL_PC->io.pc_out))
  B := MuxLookup(io.B_sel, B_SEL_XXX)(Seq(B_SEL_RS2->io.rs2_data, B_SEL_I->io.imm))
  shamt := B(4, 0)

  io.Alu_out := MuxLookup(io.Alu_op, 0.U(32.W))(
    Seq(ALU_ADD  -> (A + B),
        ALU_CP_A -> A,
        ALU_CP_B -> B,
        ALU_SLTU -> (A < B),
        ALU_SUB  -> (A - B),
        ALU_XOR  -> (A ^ B),
        ALU_SRA  -> (A.asSInt >> shamt).asUInt,
        ALU_AND  -> (A & B),
        ALU_SL   -> (A << shamt),
        ALU_OR   -> (A | B),
        ALU_SRL  -> (A >> shamt),
        ALU_SLT  -> (A.asSInt < B.asSInt)
        ))
}
