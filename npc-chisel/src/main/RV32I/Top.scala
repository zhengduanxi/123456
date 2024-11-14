import chisel3._
import chisel3.util.DecoupledIO

class TopIO(dataWidth: Int, addrWidth: Int) extends Bundle {
  // val clk_mem = Input(Clock())
  // val inst    = Input(UInt(dataWidth.W))
  val addr    = Output(UInt(addrWidth.W))
  val done    = Output(Bool())
}

class top(val dataWidth: Int, val addrWidth: Int) extends Module {
  val io = IO(new TopIO(dataWidth, addrWidth))

  val ifu     = Module(new IFU(dataWidth))
  val idu     = Module(new IDU(dataWidth))
  val exu     = Module(new EXU(dataWidth))
  val meu     = Module(new MEU(dataWidth))
  val wbu     = Module(new WBU(dataWidth))

  val mem     = Module(new MemAccess(dataWidth, addrWidth))
  val dsram   = Module(new DSRAM(dataWidth))
  val arbiter = Module(new AxiArbiter(dataWidth))
  val xbar    = Module(new Xbar(dataWidth))
  val uart    = Module(new UART(dataWidth))
  val clint   = Module(new CLINT(dataWidth))

  val mem_to_arbiter_master_interface  = Module(new AxiMasterInterface(dataWidth))
  val ifu_to_arbiter_master_interface  = Module(new AxiMasterInterface(dataWidth))
  val xbar_to_dsram_slave_interface    = Module(new AxiSlaveInterface(dataWidth))
  val xbar_to_uart_slave_interface     = Module(new AxiSlaveInterface(dataWidth))
  val xbar_to_clint_slave_interface    = Module(new AxiSlaveInterface(dataWidth))

  StageConnect(ifu.io.out, idu.io.in)
  StageConnect(idu.io.out, exu.io.in)
  StageConnect(exu.io.out, meu.io.in)
  StageConnect(meu.io.out, wbu.io.in)
  StageConnect(wbu.io.out, ifu.io.in)

  // ifu.io.inst := io.inst
  io.addr := ifu.io.addr
  io.done := ifu.io.done

  // meu <=> meu
  mem.io.meu_valid := meu.io.meu_valid
  mem.io.addr      := meu.io.addr
  mem.io.st_type   := meu.io.st_type
  mem.io.ld_type   := meu.io.ld_type
  mem.io.wr_data   := meu.io.wr_data
  meu.io.rd_data   := mem.io.rd_data
  meu.io.mem_valid := mem.io.mem_valid

  // ifu <=> master interface
  ifu.io.mm <> ifu_to_arbiter_master_interface.io.mm
  // mem <=> master interface
  mem.io.mm <> mem_to_arbiter_master_interface.io.mm
  // master interface <=> arbiter
  ifu_to_arbiter_master_interface.io.ma <> arbiter.io.ma1
  mem_to_arbiter_master_interface.io.ma <> arbiter.io.ma2
  // arbiter <=> Xbar
  arbiter.io.sa1 <> xbar.io.ax
  // Xbar <=> dsram slave interface
  xbar.io.xbar_dsram <> xbar_to_dsram_slave_interface.io.sa
  // Xbar <=> uart slave interface
  xbar.io.xbar_uart <> xbar_to_uart_slave_interface.io.sa
  // Xbar <=> clint slave interface
  xbar.io.xbar_clint <> xbar_to_clint_slave_interface.io.sa
  // dsram slave interface <=> dsram
  xbar_to_dsram_slave_interface.io.ss <> dsram.io.ss
  // uart slave interface <=> uart
  xbar_to_uart_slave_interface.io.ss <> uart.io.su
  // clint slave interface <=> clint
  xbar_to_clint_slave_interface.io.ss <> clint.io.sc

  ifu.io.ACLK    := clock
  ifu.io.ARESETn := !(reset.asBool)

  mem.io.ACLK    := clock
  mem.io.ARESETn := !(reset.asBool)

  dsram.io.ACLK    := clock
  dsram.io.ARESETn := !(reset.asBool)

  arbiter.io.ACLK    := clock
  arbiter.io.ARESETn := !(reset.asBool)

  xbar.io.ACLK    := clock
  xbar.io.ARESETn := !(reset.asBool)

  uart.io.ACLK    := clock
  uart.io.ARESETn := !(reset.asBool)

  clint.io.ACLK    := clock
  clint.io.ARESETn := !(reset.asBool)

  mem_to_arbiter_master_interface.io.ACLK     :=  clock
  mem_to_arbiter_master_interface.io.ARESETn  := !(reset.asBool)

  ifu_to_arbiter_master_interface.io.ACLK     :=  clock
  ifu_to_arbiter_master_interface.io.ARESETn  := !(reset.asBool)

  xbar_to_dsram_slave_interface.io.ACLK    :=  clock
  xbar_to_dsram_slave_interface.io.ARESETn := !(reset.asBool) 

  xbar_to_uart_slave_interface.io.ACLK    :=  clock
  xbar_to_uart_slave_interface.io.ARESETn := !(reset.asBool) 

  xbar_to_clint_slave_interface.io.ACLK    :=  clock
  xbar_to_clint_slave_interface.io.ARESETn := !(reset.asBool) 
}

object StageConnect {
  def apply[T <: Data](left: DecoupledIO[T], right:DecoupledIO[T]) = {
    val arch = "multi"

    if (arch == "single") {right.bits := left.bits}
    else if (arch == "multi") {right <> left}
  }
}

object RV32IMain extends App {
	//emitVerilog(new PC(), Array("--target-dir", "generated"))
	//emitVerilog(new RegFile(), Array("--target-dir", "generated"))
	//emitVerilog(new IFU(), Array("--target-dir", "generated"))
	//emitVerilog(new IDU(), Array("--target-dir", "generated"))
	//emitVerilog(new EXU(), Array("--target-dir", "generated"))
	emitVerilog(new top(32, 32), Array("--target-dir", "generated"))
}