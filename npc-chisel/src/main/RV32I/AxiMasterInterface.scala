import chisel3._
import chisel3.util.DecoupledIO

class AxiMasterInterface(xlen:Int) extends Module {
  val io = IO(new Bundle {
    // Global
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())

    // Master interface <=> Arbiter
    val ma      = new MessageMA(xlen)

    // Master <=> Master interface
    val mm      = new MessageMM(xlen)
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
    // Write data channel
    val WVALID  = RegInit(0.B)
    val WDATA   = RegInit(0.U(xlen.W))
    // Write response channel
    val BREADY  = RegInit(0.B)
    // Read address channel
    val ARVALID = RegInit(0.B)
    val ARADDR  = RegInit(0.U(xlen.W))
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
    rvalid := Mux(io.ma.RVALID && io.ma.RREADY, 1.B, 0.B)

    // output rdata
    rdata := Mux(io.ma.RVALID && io.ma.RREADY, io.ma.RDATA, rdata)

    // output wdone
    wdone := Mux(io.ma.BVALID && io.ma.BREADY, 1.B, 0.B)

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
    AWVALID := Mux(io.ma.AWVALID && io.ma.AWREADY, 0.B, Mux(!io.ma.AWVALID && wen_raise, 1.B, AWVALID))
    AWADDR  := Mux(wen_raise, io.mm.waddr, AWADDR)
    // AWSIZE  := Mux(wen_raise, io.mm.awsize, AWSIZE)

    //---------------------
    //Write data channel
    //---------------------
    WVALID := Mux(io.ma.WVALID && io.ma.WREADY, 0.B, Mux(!io.ma.WVALID && wen_raise, 1.B, WVALID))
    WDATA  := Mux(wen_raise, io.mm.wdata, WDATA)

    //---------------------
    //Write response channel
    //---------------------
    BREADY := Mux(io.ma.BVALID && !io.ma.BREADY, 1.B, Mux(io.ma.BREADY, 0.B, BREADY))

    //---------------------
    //Read address channel
    //---------------------
    ARVALID := Mux(io.ma.ARVALID && io.ma.ARREADY, 0.B, Mux(!io.ma.ARVALID && ren_raise, 1.B, ARVALID))
    ARADDR  := Mux(ren_raise, io.mm.raddr, ARADDR)
    // ARSIZE  := Mux(ren_raise, io.mm.arsize, ARSIZE)

    //---------------------
    //Read data channel
    //---------------------
    RREADY := Mux(io.ma.RVALID && !io.ma.RREADY, 1.B, Mux(io.ma.RREADY, 0.B, RREADY))

    io.mm.rvalid := rvalid
    io.mm.rdata  := rdata
    io.mm.wdone  := wdone

    // io.ma.AWPROT := 0.U
    io.ma.WSTRB  := io.mm.wmask((xlen/8 - 1), 0)
    // io.ma.ARPROT := 0.U

    io.ma.AWID    := 0.U
    io.ma.AWLEN   := 0.U
    io.ma.AWSIZE  := 0.U
    io.ma.AWBURST := 0.U
    io.ma.WLAST   := 0.B
    io.ma.ARID    := 0.U
    io.ma.ARLEN   := 0.U
    io.ma.ARSIZE  := 0.U
    io.ma.ARBURST := 0.U

    io.ma.AWVALID := AWVALID
    io.ma.AWADDR  := AWADDR
    // io.ma.AWSIZE  := AWSIZE
    io.ma.WVALID  := WVALID
    io.ma.WDATA   := WDATA
    io.ma.BREADY  := BREADY
    io.ma.ARVALID := ARVALID
    io.ma.ARADDR  := ARADDR
    // io.ma.ARSIZE  := ARSIZE
    io.ma.RREADY  := RREADY
  }
}
