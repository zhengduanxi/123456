import chisel3._
import chisel3.util.Decoupled

class EXU(xlen: Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new MessageDE(xlen)))
    val out =  Decoupled(new MessageEM(xlen))
  })

  val immGen  = Module(new ImmGen(xlen))
  val alu     = Module(new Alu(xlen, xlen))
  val brcond  = Module(new BrCond(xlen))

  immGen.io.inst    := io.in.bits.inst
  immGen.io.Imm_sel := io.in.bits.Imm_sel

  alu.io.imm      := immGen.io.imm
  alu.io.rs1_data := io.in.bits.rs1_data
  alu.io.rs2_data := io.in.bits.rs2_data
  alu.io.pc_out   := io.in.bits.pc
  alu.io.Alu_op   := io.in.bits.Alu_op
  alu.io.A_sel    := io.in.bits.A_sel
  alu.io.B_sel    := io.in.bits.B_sel

  brcond.io.br_type  := io.in.bits.br_type
  brcond.io.rs1_data := io.in.bits.rs1_data
  brcond.io.rs2_data := io.in.bits.rs2_data

  io.out.bits.inst    := io.in.bits.inst
  io.out.bits.pc      := io.in.bits.pc

  io.out.bits.PC_sel  := io.in.bits.PC_sel 
  io.out.bits.st_type := io.in.bits.st_type
  io.out.bits.ld_type := io.in.bits.ld_type
  io.out.bits.wb_sel  := io.in.bits.wb_sel 
  io.out.bits.csr_cmd := io.in.bits.csr_cmd
  io.out.bits.wb_en   := io.in.bits.wb_en
  io.out.bits.br_taken := brcond.io.br_taken

  io.out.bits.Alu_out  := alu.io.Alu_out
  io.out.bits.wb_addr  := io.in.bits.wb_addr
  io.out.bits.rs2_data := io.in.bits.rs2_data

  io.out.valid := io.in.valid
  io.in.ready  := 1.B
}
