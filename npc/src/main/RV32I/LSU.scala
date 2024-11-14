import chisel3._
import chisel3.util._
import Control._

class MEU(xlen: Int)extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new MessageEM(xlen)))
    val out = Decoupled(new MessageMW(xlen))

    val meu_valid = Output(Bool())
    val addr      = Output(UInt(xlen.W))
    val st_type   = Output(UInt(2.W))
    val ld_type   = Output(UInt(3.W))
    val wr_data   = Output(UInt(xlen.W))
    val rd_data   = Input(UInt(xlen.W))
    val mem_valid = Input(Bool())
  })

  io.out.bits.inst    := io.in.bits.inst 
  io.out.bits.pc      := io.in.bits.pc 

  io.out.bits.PC_sel  := io.in.bits.PC_sel
  io.out.bits.wb_sel  := io.in.bits.wb_sel 
  io.out.bits.csr_cmd := io.in.bits.csr_cmd
  io.out.bits.wb_en   := io.in.bits.wb_en
  io.out.bits.br_taken := io.in.bits.br_taken

  io.out.bits.Alu_out := io.in.bits.Alu_out
  io.out.bits.wb_addr := io.in.bits.wb_addr
  io.out.bits.rd_data := io.rd_data

  io.meu_valid := io.in.valid
  io.addr      := io.in.bits.Alu_out
  io.st_type   := Mux(io.in.valid, io.in.bits.st_type, ST_XXX)
  io.ld_type   := Mux(io.in.valid, io.in.bits.ld_type, LD_XXX)
  io.wr_data   := io.in.bits.rs2_data

  io.out.valid := io.in.valid && io.mem_valid
  io.in.ready  := 1.B
}
