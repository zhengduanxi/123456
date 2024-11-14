import chisel3._
import Control._
import chisel3.util.MuxLookup

class RfIO(dataWidth: Int, addrWidth: Int) extends Bundle {
  val wb_en    = Input(Bool())
  val Alu_out  = Input(UInt(dataWidth.W))
  val csr_out  = Input(UInt(dataWidth.W))
  val pc_4     = Input(UInt(addrWidth.W))
  val wb_sel   = Input(UInt(2.W))
  val wb_addr  = Input(UInt(5.W))
  val rs1_addr = Input(UInt(5.W))
  val rs2_addr = Input(UInt(5.W))
  val rd_data  = Input(UInt(dataWidth.W))  // 从内存中读取的数据
  val rs1_data = Output(UInt(dataWidth.W))
  val rs2_data = Output(UInt(dataWidth.W))
  val r10      = Output(UInt(dataWidth.W))
}

class RegisterFile(val dataWidth: Int, val addrWidth: Int) extends Module {
  val io = IO(new RfIO(dataWidth, addrWidth))

  val rf = Reg(Vec(32, UInt(dataWidth.W)))
  val wb_data = Wire(UInt(dataWidth.W))

  io.rs1_data := Mux(io.rs1_addr.orR, rf(io.rs1_addr), 0.U)
  io.rs2_data := Mux(io.rs2_addr.orR, rf(io.rs2_addr), 0.U)
  io.r10      := rf(10)

  wb_data := MuxLookup(io.wb_sel, io.Alu_out)(
    Seq(WB_SEL_PC_4 -> io.pc_4,
        WB_SEL_ALU  -> io.Alu_out,
        WB_SEL_MEM  -> io.rd_data,
        WB_SEL_CSR  -> io.csr_out
      ))

  rf(io.wb_addr) := Mux(io.wb_en, Mux(io.wb_addr.orR, wb_data, 0.U), rf(io.wb_addr))
}
