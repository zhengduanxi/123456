import chisel3._
import chisel3.util.DecoupledIO

class AXI4MasterInterface(xlen:Int) extends Module {
  val io = IO(new Bundle {
    // Global
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())

    // Master interface <=> Arbiter
    val ma      = new MessageAXI4MasterInterface(xlen)

    // Master <=> Master interface
    val mm      = new MessageAXI4ToMaster(xlen)
  })

  // ARESETn is effective at low electrical levels
  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    /* Master <=> Master interface */
    // read
    val rdata   = RegInit(0.U(xlen.W))
    val rvalid  = RegInit(0.B)
    // write
    val wdone   = RegInit(0.B)

    /* Master interface <=> Arbiter */
    // Write address channel
    val AWVALID = RegInit(0.B)
    val AWADDR  = RegInit(0.U(xlen.W))
    val AWSIZE  = RegInit(0.U(3.W))
    // Write data channel
    val WVALID  = RegInit(0.B)
    val WDATA   = RegInit(0.U(xlen.W))
    // Write response channel
    val BREADY  = RegInit(0.B)
    // Read address channel
    val ARVALID = RegInit(0.B)
    val ARADDR  = RegInit(0.U(xlen.W))
    val ARSIZE  = RegInit(0.U(3.W))
    // Read data channel
    val RREADY  = RegInit(0.B)

    // raising edge of wen
    val wen_raise = Wire(Bool())
    val wen_cur   = RegInit(0.B)
    val wen_pre   = RegInit(0.B)

    // raising edge of ren
    val ren_raise = Wire(Bool())
    val ren_cur   = RegInit(0.B)
    val ren_pre   = RegInit(0.B)


    // output rvalid
    rvalid := Mux(io.ma.rvalid && io.ma.rready, 1.B, 0.B)

    // output rdata
    rdata  := Mux(io.ma.rvalid && io.ma.rready, io.ma.rdata, rdata)

    // output wdone
    wdone  := Mux(io.ma.bvalid && io.ma.bready, 1.B, 0.B)

    // get raiseing edge of wen
    wen_raise := (!wen_pre) && wen_cur
    wen_cur   := io.mm.wen
    wen_pre   := wen_cur

    // get raising edge of wen
    ren_raise := (!ren_pre) && ren_cur
    ren_cur   := io.mm.ren
    ren_pre   := ren_cur

    //---------------------
    //Write address channel
    //---------------------
    AWVALID := Mux(io.ma.awvalid && io.ma.awready, 0.B, Mux(!io.ma.awvalid && wen_raise, 1.B, AWVALID))
    AWADDR  := Mux(wen_raise, io.mm.waddr, AWADDR)
    AWSIZE  := Mux(wen_raise, io.mm.awsize, AWSIZE)

    //---------------------
    //Write data channel
    //---------------------
    WVALID := Mux(io.ma.wvalid && io.ma.wready, 0.B, Mux(!io.ma.wvalid && wen_raise, 1.B, WVALID))
    WDATA  := Mux(wen_raise, io.mm.wdata, WDATA)

    //---------------------
    //Write response channel
    //---------------------
    BREADY := Mux(io.ma.bvalid && !io.ma.bready, 1.B, Mux(io.ma.bready, 0.B, BREADY))

    //---------------------
    //Read address channel
    //---------------------
    ARVALID := Mux(io.ma.arvalid && io.ma.arready, 0.B, Mux(!io.ma.arvalid && ren_raise, 1.B, ARVALID))
    ARADDR  := Mux(ren_raise, io.mm.raddr, ARADDR)
    ARSIZE  := Mux(ren_raise, io.mm.arsize, ARSIZE)

    //---------------------
    //Read data channel
    //---------------------
    RREADY := Mux(io.ma.rvalid && !io.ma.rready, 1.B, Mux(io.ma.rready, 0.B, RREADY))

    io.mm.rvalid  := rvalid
    io.mm.rdata   := rdata
    io.mm.wdone   := wdone

    // Write address channel
    io.ma.awid    := 0.U    // default is zero
    io.ma.awlen   := 0.U    // total number of transfers: Length = AWLEN + 1
    io.ma.awburst := 1.U    // INCR burst type

    // Write data channel
    io.ma.wstrb   := io.mm.wmask((xlen/8 - 1), 0)
    io.ma.wlast   := 1.B

    // Read address channel
    io.ma.arid    := 0.U    // default is zero
    io.ma.arlen   := 0.U    // total number of transfers: Length = AWLEN + 1
    io.ma.arburst := 1.U    // INCR burst type

    io.ma.awvalid := AWVALID
    io.ma.awaddr  := AWADDR
    io.ma.awsize  := AWSIZE
    io.ma.wvalid  := WVALID
    io.ma.wdata   := WDATA
    io.ma.bready  := BREADY
    io.ma.arvalid := ARVALID
    io.ma.araddr  := ARADDR
    io.ma.arsize  := ARSIZE
    io.ma.rready  := RREADY
  }
}

