import chisel3._
import chisel3.util._
import Control._
import chisel3.util.MuxLookup

class get_cpu_pc(xlen:Int) extends BlackBox {
  val io = IO(new Bundle {
    val PCNext = Input(UInt(xlen.W))
  })
}

class PCIO(dataWidth: Int, addrWidth: Int) extends Bundle {
  val PC_sel   = Input(UInt(2.W))
  val Alu_out  = Input(UInt(dataWidth.W))
  val br_taken = Input(Bool())
  val csr_out  = Input(UInt(dataWidth.W))
  val wen      = Input(Bool())
  val pc_out   = Output(UInt(addrWidth.W))
  val pc_4     = Output(UInt(addrWidth.W))
}

class PC(val dataWidth: Int, val addrWidth: Int) extends Module {
  // override def resetType: Module.ResetType.Type = Module.ResetType.Synchronous

  val io = IO(new PCIO(dataWidth, addrWidth))
  val pc_in = Wire(UInt(addrWidth.W))
  val pc = Reg(UInt(addrWidth.W))
  val get_cpu_pc  = Module(new get_cpu_pc(dataWidth))

  io.pc_4 := pc + 4.U

  pc_in := MuxLookup(Cat(io.br_taken, io.PC_sel), pc + 4.U)(
    Seq(Cat(0.B, PC_4)   -> (pc + 4.U),
        Cat(0.B, PC_ALU) -> io.Alu_out,
        Cat(0.B, PC_CSR) -> io.csr_out,
        Cat(1.B, PC_4)   -> io.Alu_out,
        Cat(1.B, PC_ALU) -> io.Alu_out,
        Cat(1.B, PC_CSR) -> io.Alu_out
      ))

  // when(io.br_taken === 0.B & io.PC_sel === PC_CSR) {
  //   printf(p"pc_in = 0x${Hexadecimal(pc_in)}\n")
  // }

  pc := Mux(reset.asBool, "h80000000".U(addrWidth.W), Mux(io.wen, pc_in, pc))
  get_cpu_pc.io.PCNext := pc_in
  io.pc_out := pc
}
