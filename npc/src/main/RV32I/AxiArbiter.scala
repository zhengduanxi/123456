import chisel3._
import chisel3.util._

class AxiArbiter(xlen:Int) extends Module {
  val io = IO(new Bundle {
    // Global
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())
    // Master interface <=> Arbiter
    val ma1     = Flipped(new MessageMA(xlen))
    val ma2     = Flipped(new MessageMA(xlen))

    // Arbiter <=> Slave interface
    val sa1     = new MessageMA(xlen)
  })

  // ARESETn is effective at low electrical levels
  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val done = Wire(Bool())
    done := (io.sa1.BVALID && io.sa1.BREADY) || (io.sa1.RVALID && io.sa1.RREADY)

    // states
    val sIDEL :: sMASTER1 :: sMASTER2 :: Nil = Enum(3)

    val curr_status = RegInit(sIDEL)
    val next_status = WireDefault(sIDEL)

    // status transfer
    curr_status := next_status
    switch(curr_status) {
      is(sIDEL) {
        when (io.ma1.AWVALID || io.ma1.ARVALID) {
          next_status := sMASTER1
        } .elsewhen (io.ma2.AWVALID || io.ma2.ARVALID) {
          next_status := sMASTER2
        } .otherwise {
          next_status := sIDEL
        }
      }
      is(sMASTER1) {
        when (done) {
          next_status := sIDEL
        } .otherwise {
          next_status := sMASTER1
        }
      }
      is(sMASTER2) {
        when (done) {
          next_status := sIDEL
        } .otherwise {
          next_status := sMASTER2
        }
      }
    }

    // process request of master 1 and master 2
    // Write address channel
    io.sa1.AWVALID := Mux(curr_status===sMASTER1, io.ma1.AWVALID, Mux(curr_status===sMASTER2, io.ma2.AWVALID, 0.B))
    io.sa1.AWADDR  := Mux(curr_status===sMASTER1, io.ma1.AWADDR,  Mux(curr_status===sMASTER2, io.ma2.AWADDR,  0.U))
    // io.sa1.AWPROT  := Mux(curr_status===sMASTER1, io.ma1.AWPROT, Mux(curr_status===sMASTER2, io.ma2.AWPROT, 0.U))
    io.sa1.AWID    := Mux(curr_status===sMASTER1, io.ma1.AWID,    Mux(curr_status===sMASTER2, io.ma2.AWID,    0.U))
    io.sa1.AWLEN   := Mux(curr_status===sMASTER1, io.ma1.AWLEN,   Mux(curr_status===sMASTER2, io.ma2.AWLEN,   0.U))
    io.sa1.AWSIZE  := Mux(curr_status===sMASTER1, io.ma1.AWSIZE,  Mux(curr_status===sMASTER2, io.ma2.AWSIZE,  0.U))
    io.sa1.AWBURST := Mux(curr_status===sMASTER1, io.ma1.AWBURST, Mux(curr_status===sMASTER2, io.ma2.AWBURST, 0.U))
    // Write data channel
    io.sa1.WVALID  := Mux(curr_status===sMASTER1, io.ma1.WVALID,  Mux(curr_status===sMASTER2, io.ma2.WVALID,  0.B))
    io.sa1.WDATA   := Mux(curr_status===sMASTER1, io.ma1.WDATA,   Mux(curr_status===sMASTER2, io.ma2.WDATA,   0.U))
    io.sa1.WSTRB   := Mux(curr_status===sMASTER1, io.ma1.WSTRB,   Mux(curr_status===sMASTER2, io.ma2.WSTRB,   0.U))
    io.sa1.WLAST   := Mux(curr_status===sMASTER1, io.ma1.WLAST,   Mux(curr_status===sMASTER2, io.ma2.WLAST,   0.B))
    // Write response channel
    io.sa1.BREADY  := Mux(curr_status===sMASTER1, io.ma1.BREADY,  Mux(curr_status===sMASTER2, io.ma2.BREADY,  0.B))
    // Read address channel
    io.sa1.ARVALID := Mux(curr_status===sMASTER1, io.ma1.ARVALID, Mux(curr_status===sMASTER2, io.ma2.ARVALID, 0.B))
    io.sa1.ARADDR  := Mux(curr_status===sMASTER1, io.ma1.ARADDR,  Mux(curr_status===sMASTER2, io.ma2.ARADDR,  0.U))
    // io.sa1.ARPROT  := Mux(curr_status===sMASTER1, io.ma1.ARPROT, Mux(curr_status===sMASTER2, io.ma2.ARPROT, 0.U))
    io.sa1.ARID    := Mux(curr_status===sMASTER1, io.ma1.ARID,    Mux(curr_status===sMASTER2, io.ma2.ARID,    0.U))
    io.sa1.ARLEN   := Mux(curr_status===sMASTER1, io.ma1.ARLEN,   Mux(curr_status===sMASTER2, io.ma2.ARLEN,   0.U))
    io.sa1.ARSIZE  := Mux(curr_status===sMASTER1, io.ma1.ARSIZE,  Mux(curr_status===sMASTER2, io.ma2.ARSIZE,  0.U))
    io.sa1.ARBURST := Mux(curr_status===sMASTER1, io.ma1.ARBURST, Mux(curr_status===sMASTER2, io.ma2.ARBURST, 0.U))
    // Read data channel
    io.sa1.RREADY  := Mux(curr_status===sMASTER1, io.ma1.RREADY,  Mux(curr_status===sMASTER2, io.ma2.RREADY,  0.B))

    // process response of slave1 to master1
    io.ma1.AWREADY := Mux(curr_status===sMASTER1, io.sa1.AWREADY, 0.B)
    // Write data channel
    io.ma1.WREADY  := Mux(curr_status===sMASTER1, io.sa1.WREADY,  0.B)
    // Write response channel
    io.ma1.BVALID  := Mux(curr_status===sMASTER1, io.sa1.BVALID,  0.B)
    io.ma1.BRESP   := Mux(curr_status===sMASTER1, io.sa1.BRESP,   0.U)
    io.ma1.BID     := Mux(curr_status===sMASTER1, io.sa1.BID,     0.U)
    // Read address channel
    io.ma1.ARREADY := Mux(curr_status===sMASTER1, io.sa1.ARREADY, 0.B)
    // Read data channel
    io.ma1.RVALID  := Mux(curr_status===sMASTER1, io.sa1.RVALID,  0.B)
    io.ma1.RDATA   := Mux(curr_status===sMASTER1, io.sa1.RDATA,   0.U)
    io.ma1.RRESP   := Mux(curr_status===sMASTER1, io.sa1.RRESP,   0.U)
    io.ma1.RLAST   := Mux(curr_status===sMASTER1, io.sa1.RLAST,   0.B)
    io.ma1.RID     := Mux(curr_status===sMASTER1, io.sa1.RID,     0.U)

    // process response of slave1 to master2
    // Write address channel
    io.ma2.AWREADY := Mux(curr_status===sMASTER2, io.sa1.AWREADY, 0.B)
    // Write data channel
    io.ma2.WREADY  := Mux(curr_status===sMASTER2, io.sa1.WREADY,  0.B)
    // Write response channel
    io.ma2.BVALID  := Mux(curr_status===sMASTER2, io.sa1.BVALID,  0.B)
    io.ma2.BRESP   := Mux(curr_status===sMASTER2, io.sa1.BRESP,   0.U)
    io.ma2.BID     := Mux(curr_status===sMASTER2, io.sa1.BID,     0.U)
    // Read address channel
    io.ma2.ARREADY := Mux(curr_status===sMASTER2, io.sa1.ARREADY, 0.B)
    // Read data channel
    io.ma2.RVALID  := Mux(curr_status===sMASTER2, io.sa1.RVALID,  0.B)
    io.ma2.RDATA   := Mux(curr_status===sMASTER2, io.sa1.RDATA,   0.U)
    io.ma2.RRESP   := Mux(curr_status===sMASTER2, io.sa1.RRESP,   0.U)
    io.ma2.RLAST   := Mux(curr_status===sMASTER2, io.sa1.RLAST,   0.B)
    io.ma2.RID     := Mux(curr_status===sMASTER2, io.sa1.RID,     0.U)
  }
}
