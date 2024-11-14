import chisel3._
import chisel3.util._

class IDU(xlen: Int) extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new MessageFD(xlen)))
    val out = Decoupled(new MessageDE(xlen))
  })

  val decoder = Module(new Decoder(xlen))

  decoder.io.inst := io.in.bits.inst
  decoder.io.pc := io.in.bits.pc
  decoder.io.r10 := io.in.bits.r10

  io.out.bits.inst    := io.in.bits.inst
  io.out.bits.pc      := io.in.bits.pc
  io.out.bits.PC_sel  := decoder.io.PC_sel
  io.out.bits.Imm_sel := decoder.io.Imm_sel
  io.out.bits.Alu_op  := decoder.io.Alu_op
  io.out.bits.st_type := decoder.io.st_type
  io.out.bits.ld_type := decoder.io.ld_type
  io.out.bits.br_type := decoder.io.br_type
  io.out.bits.A_sel   := decoder.io.A_sel
  io.out.bits.B_sel   := decoder.io.B_sel
  io.out.bits.wb_sel  := decoder.io.wb_sel
  io.out.bits.csr_cmd := decoder.io.csr_cmd
  io.out.bits.wb_en   := decoder.io.wb_en

  io.out.bits.wb_addr  := io.in.bits.wb_addr
  io.out.bits.rs1_data := io.in.bits.rs1_data
  io.out.bits.rs2_data := io.in.bits.rs2_data

  io.out.valid := io.in.valid
  io.in.ready  := 1.B
}
