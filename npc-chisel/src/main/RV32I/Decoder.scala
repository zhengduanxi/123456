import chisel3._
import chisel3.util._
import Control._

class DecoderIO(dataWidth: Int) extends Bundle {
  val inst    = Input(UInt(dataWidth.W))
  val pc      = Input(UInt(dataWidth.W))
  val r10     = Input(UInt(dataWidth.W))
  val PC_sel  = Output(UInt(2.W))
  val Imm_sel = Output(UInt(3.W))
  val Alu_op  = Output(UInt(4.W))
  val st_type = Output(UInt(2.W))
  val ld_type = Output(UInt(3.W))
  val br_type = Output(UInt(3.W))
  val A_sel   = Output(UInt(1.W))
  val B_sel   = Output(UInt(1.W))
  val wb_sel  = Output(UInt(2.W))
  val csr_cmd = Output(UInt(3.W))
  val wb_en   = Output(Bool())
}

class Ebreak extends BlackBox {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val pc   = Input(UInt(32.W))
    val r10  = Input(UInt(32.W))
  })
}

class Decoder(val dataWidth: Int) extends Module {
  val io = IO(new DecoderIO(dataWidth))

  val ebreak = Module(new Ebreak)
  ebreak.io.inst := io.inst
  ebreak.io.pc := io.pc
  ebreak.io.r10 := io.r10

  val ctrl_signals = ListLookup(io.inst, default, map) 

  io.PC_sel  := ctrl_signals(0)
  io.Imm_sel := ctrl_signals(1)
  io.Alu_op  := ctrl_signals(2)
  io.st_type := ctrl_signals(3)
  io.ld_type := ctrl_signals(4)
  io.br_type := ctrl_signals(5)
  io.A_sel   := ctrl_signals(6)
  io.B_sel   := ctrl_signals(7)
  io.wb_sel  := ctrl_signals(8)
  io.csr_cmd := ctrl_signals(9)
  io.wb_en   := ctrl_signals(10)
}
