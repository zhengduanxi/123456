import chisel3._
import chisel3.util._

class AxiSlaveInterface(xlen:Int) extends Module {
  val io = IO(new Bundle {
    // Global
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())

    // Slave interface <=> Arbiter
    val sa      = Flipped(new MessageMA(xlen))

    // Slave <=> Slave interface
    val ss      = Flipped(new MessageMM(xlen))
  })

  // ARESETn is effective at high electrical levels
  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    /* Slave <=> Slave interface */
   val ren   = RegInit(0.B)
   val raddr = RegInit(0.U(xlen.W))
   val wen   = RegInit(0.B)
   val waddr = RegInit(0.U(xlen.W))
   val wdata = RegInit(0.U(xlen.W))

    /* Slave interface <=> Arbiter */
    // Write address channel
    val AWREADY = RegInit(0.B)
    // Write data channel
    val WREADY  = RegInit(0.B)
    // Write response channel
    val BVALID  = RegInit(0.B)
    val BRESP   = RegInit(0.U(2.W))
    // Read address channel
    val ARREADY = RegInit(0.B)
    // Read data channel
    val RVALID  = RegInit(0.B)
    val RDATA   = RegInit(0.U(xlen.W))
    val RRESP   = RegInit(0.U(2.W))


    // output ren
    ren := Mux(io.sa.ARREADY && io.sa.ARVALID, 1.B, 0.B)

    // get read address
    raddr := Mux(!io.sa.ARREADY && io.sa.ARVALID, io.sa.ARADDR, raddr)

    // output wen
    wen := Mux((io.sa.AWREADY && io.sa.AWVALID) && (io.sa.WREADY && io.sa.WVALID), 1.B, 0.B)

    // get write address 
    waddr := Mux(!io.sa.AWREADY && io.sa.AWVALID && io.sa.WVALID, io.sa.AWADDR, waddr)

    // get write data
    wdata := Mux(!io.sa.WREADY && io.sa.WVALID && io.sa.AWVALID, io.sa.WDATA, wdata)

    //---------------------
    //Write address channel
    //---------------------
    AWREADY := Mux(!io.sa.AWREADY && io.sa.AWVALID && io.sa.WVALID, 1.B, Mux(io.sa.BVALID && io.sa.BREADY, 0.B, AWREADY))

    //---------------------
    //Write data channel
    //---------------------
    WREADY := Mux(!io.sa.WREADY && io.sa.WVALID && io.sa.AWVALID, 1.B, 0.B)

    //---------------------
    //Write response channel
    //---------------------
    BVALID := Mux(!io.sa.BVALID && io.ss.wdone, 1.B, Mux(io.sa.BREADY && io.sa.BVALID, 0.B, BVALID))
    BRESP  := Mux(!io.sa.BVALID && io.ss.wdone, 0.U, BRESP)

    //---------------------
    //Read address channel
    //---------------------
    ARREADY := Mux(!io.sa.ARREADY && io.sa.ARVALID, 1.B, 0.B)

    //---------------------
    //Read data channel
    //---------------------
    RVALID := Mux(!io.sa.RVALID && io.ss.rvalid, 1.B, Mux(io.sa.RVALID && io.sa.RREADY, 0.B, RVALID))
    RRESP := Mux(!io.sa.RVALID && io.ss.rvalid, 0.U, RRESP)
    RDATA := Mux(!io.sa.RVALID && io.ss.rvalid, io.ss.rdata, RDATA)

    io.ss.ren   := ren
    io.ss.raddr := raddr
    // io.ss.arsize:= arsize
    io.ss.wen   := wen
    io.ss.waddr := waddr
    io.ss.wdata := wdata
    io.ss.wmask := Cat(0.U((8-xlen/8).W), io.sa.WSTRB)
    // io.ss.awsize:= awsize

    io.sa.AWREADY := AWREADY
    io.sa.WREADY  := WREADY
    io.sa.BVALID  := BVALID
    io.sa.BRESP   := BRESP
    io.sa.ARREADY := ARREADY
    io.sa.RVALID  := RVALID
    io.sa.RDATA   := RDATA
    io.sa.RRESP   := RRESP

    io.sa.BID     := 0.U
    io.sa.RLAST   := 0.B
    io.sa.RID     := 0.U
  }
}
