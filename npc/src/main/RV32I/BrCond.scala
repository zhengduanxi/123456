import chisel3._
import Control._
import chisel3.util.MuxLookup

class BrCondIO(dataWidth: Int) extends Bundle {
  val br_type  = Input(UInt(3.W))
  val rs1_data = Input(UInt(dataWidth.W))
  val rs2_data = Input(UInt(dataWidth.W))
  val br_taken = Output(Bool())
}

class BrCond(val dataWidth: Int) extends Module {
  val io = IO(new BrCondIO(dataWidth))

  val eq  = io.rs1_data === io.rs2_data
  val neq = !eq
  val lt  = io.rs1_data.asSInt < io.rs2_data.asSInt
  val ge  = !lt
  val ltu = io.rs1_data < io.rs2_data
  val geu = !ltu

  io.br_taken := MuxLookup(io.br_type, 0.U.asBool)(
    Seq(BR_BNE  -> neq,
        BR_BEQ  -> eq,
        BR_BGE  -> ge,
        BR_BGEU -> geu,
        BR_BLTU -> ltu,
        BR_BLT  -> lt
    ))
}
