import chisel3._
import chisel3.util._
import Control._

class ImmGenIo(dataWidth: Int) extends Bundle {
  val inst    = Input(UInt(dataWidth.W))
  val Imm_sel = Input(UInt(3.W))
  val imm     = Output(UInt(dataWidth.W))
}

class ImmGen(val dataWidth: Int) extends Module {
  val io = IO(new ImmGenIo(dataWidth))

  val imm_I = io.inst(31, 20).asSInt
  val imm_U = Cat(io.inst(31, 12), 0.U(12.W)).asSInt
  val imm_J = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W)).asSInt
  val imm_S = Cat(io.inst(31, 25), io.inst(11, 7)).asSInt
  val imm_B = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)).asSInt

  io.imm := MuxLookup(io.Imm_sel, 0.S(dataWidth.W))(
    Seq(IMM_I -> imm_I,
        IMM_U -> imm_U,
        IMM_J -> imm_J,
        IMM_S -> imm_S,
        IMM_B -> imm_B,
      )).asUInt
}
