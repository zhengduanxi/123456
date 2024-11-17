import chisel3._
import chisel3.util._

class difftest_ok(xlen:Int) extends BlackBox {
  val io = IO(new Bundle {
    val valid = Input(UInt(xlen.W))
  })
}

class IFU(xlen:Int) extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool())
    val addr = Output(UInt(xlen.W))

    val in  = Flipped(Decoupled(new MessageWI(xlen)))
    val out = Decoupled(new MessageFD(xlen))

    // IFU <=> Master interface
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())
    val mm      = Flipped(new MessageAXI4ToMaster(xlen))
  })
  
  val pc = Module(new PC(xlen, xlen))
  val rf = Module(new RegisterFile(xlen, xlen))
  val difftest_ok = Module(new difftest_ok(xlen))

  val inst       = Wire(UInt(xlen.W))
  val inst_valid = Wire(Bool())
  val ren        = Wire(Bool())
  val done       = Wire(Bool())

  // synchronize with outside clock
  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val _inst = RegInit(0.U(xlen.W))
    val _inst_valid = RegInit(0.B)

    _inst := Mux(io.mm.rvalid, io.mm.rdata, _inst)
    _inst_valid := Mux(!_inst_valid && io.mm.rvalid, 1.B, Mux(_inst_valid && io.in.valid, 0.B, _inst_valid))

    val ARESETn_raise = Wire(Bool())
    val ARESETn_cur   = RegInit(0.B)
    val ARESETn_pre   = RegInit(0.B)

    ARESETn_cur   := io.ARESETn.asBool
    ARESETn_pre   := ARESETn_cur
    ARESETn_raise := ~ARESETn_pre & ARESETn_cur

    val _ren = RegInit(0.B)
    _ren := Mux(ARESETn_raise, 1.B, Mux(_inst_valid && io.in.valid, 1.B, 0.B))

    val _done = RegInit(0.B)
    _done := Mux(_inst_valid && io.in.valid, 1.B, 0.B)

    inst       := _inst
    inst_valid := _inst_valid
    ren        := _ren
    done       := _done
  }


  io.addr              := pc.io.pc_out

  io.out.bits.pc       := pc.io.pc_out
  io.out.bits.inst     := inst
  io.out.bits.wb_addr  := inst(11, 7)
  io.out.bits.rs1_data := rf.io.rs1_data
  io.out.bits.rs2_data := rf.io.rs2_data
  io.out.bits.r10      := rf.io.r10
  
  rf.io.wb_en    := io.in.bits.wb_en & io.in.valid
  rf.io.Alu_out  := io.in.bits.Alu_out
  rf.io.csr_out  := io.in.bits.csr_out
  rf.io.pc_4     := pc.io.pc_4
  rf.io.wb_sel   := io.in.bits.wb_sel
  rf.io.wb_addr  := io.in.bits.wb_addr
  rf.io.rs1_addr := inst(19, 15)
  rf.io.rs2_addr := inst(24, 20)
  rf.io.rd_data  := io.in.bits.rd_data

  pc.io.PC_sel   := io.in.bits.PC_sel
  pc.io.br_taken := io.in.bits.br_taken
  pc.io.csr_out  := io.in.bits.csr_out
  pc.io.Alu_out  := io.in.bits.Alu_out
  pc.io.wen      := inst_valid & io.in.valid

  difftest_ok.io.valid := io.in.valid

  io.done := done
  
  io.mm.ren   := ren
  io.mm.raddr := pc.io.pc_out
  io.mm.arsize := (xlen/8 - 1).U
  // do not need to write
  io.mm.wen   := 0.B
  io.mm.waddr := 0.U
  io.mm.wdata := 0.U
  io.mm.wmask := 0.U
  io.mm.awsize := (xlen/8 - 1).U

  io.out.valid := inst_valid
  io.in.ready  := 1.B
}
