import chisel3._
import chisel3.util._

class WBU(xlen: Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new MessageMW(xlen)))
    val out = Decoupled(new MessageWI(xlen))
  })
  
  val csr = Module(new CSR(xlen))

  csr.io.inst     := io.in.bits.inst
  csr.io.pc       := io.in.bits.pc
  csr.io.csr_cmd  := io.in.bits.csr_cmd
  csr.io.csr_data := io.in.bits.Alu_out
  csr.io.valid    := io.in.valid

  io.out.bits.PC_sel  := io.in.bits.PC_sel
  io.out.bits.wb_sel  := io.in.bits.wb_sel
  io.out.bits.wb_en   := io.in.bits.wb_en
  io.out.bits.br_taken := io.in.bits.br_taken
  io.out.bits.csr_out := csr.io.csr_out
  io.out.bits.Alu_out := io.in.bits.Alu_out
  io.out.bits.wb_addr := io.in.bits.wb_addr
  io.out.bits.rd_data := io.in.bits.rd_data

  io.out.valid := io.in.valid
  io.in.ready  := 1.B
}
