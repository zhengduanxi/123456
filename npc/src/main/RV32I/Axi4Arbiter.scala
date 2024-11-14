import chisel3._
import chisel3.util._

class AXI4Arbiter(xlen:Int) extends Module {
  val io = IO(new Bundle {
    // Global
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())
    // Master interface <=> Arbiter
    val slave1  = Flipped(new MessageAXI4MasterInterface(xlen))
    val slave2  = Flipped(new MessageAXI4MasterInterface(xlen))

    // Arbiter <=> Slave interface
    val master1 = new MessageAXI4MasterInterface(xlen)
  })

  // ARESETn is effective at low electrical levels
  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val done = Wire(Bool())
    done := (io.master1.bvalid && io.master1.bready) || (io.master1.rvalid && io.master1.rready)

    // states
    val sIDEL :: sMASTER1 :: sMASTER2 :: Nil = Enum(3)

    val curr_status = RegInit(sIDEL)
    val next_status = WireDefault(sIDEL)

    // status transfer
    curr_status := next_status
    switch(curr_status) {
      is(sIDEL) {
        when (io.slave1.awvalid || io.slave1.arvalid) {
          next_status := sMASTER1
        } .elsewhen (io.slave2.awvalid || io.slave2.arvalid) {
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
    io.master1.awvalid := Mux(curr_status===sMASTER1, io.slave1.awvalid, Mux(curr_status===sMASTER2, io.slave2.awvalid, 0.B))
    io.master1.awaddr  := Mux(curr_status===sMASTER1, io.slave1.awaddr,  Mux(curr_status===sMASTER2, io.slave2.awaddr,  0.U))
    io.master1.awid    := Mux(curr_status===sMASTER1, io.slave1.awid,    Mux(curr_status===sMASTER2, io.slave2.awid,    0.U))
    io.master1.awlen   := Mux(curr_status===sMASTER1, io.slave1.awlen,   Mux(curr_status===sMASTER2, io.slave2.awlen,   0.U))
    io.master1.awsize  := Mux(curr_status===sMASTER1, io.slave1.awsize,  Mux(curr_status===sMASTER2, io.slave2.awsize,  0.U))
    io.master1.awburst := Mux(curr_status===sMASTER1, io.slave1.awburst, Mux(curr_status===sMASTER2, io.slave2.awburst, 1.U))
    // Write data channel
    io.master1.wvalid  := Mux(curr_status===sMASTER1, io.slave1.wvalid,  Mux(curr_status===sMASTER2, io.slave2.wvalid,  0.B))
    io.master1.wdata   := Mux(curr_status===sMASTER1, io.slave1.wdata,   Mux(curr_status===sMASTER2, io.slave2.wdata,   0.U))
    io.master1.wstrb   := Mux(curr_status===sMASTER1, io.slave1.wstrb,   Mux(curr_status===sMASTER2, io.slave2.wstrb,   0.U))
    io.master1.wlast   := Mux(curr_status===sMASTER1, io.slave1.wlast,   Mux(curr_status===sMASTER2, io.slave2.wlast,   1.B))
    // Write response channel
    io.master1.bready  := Mux(curr_status===sMASTER1, io.slave1.bready,  Mux(curr_status===sMASTER2, io.slave2.bready,  0.B))
    // Read address channel
    io.master1.arvalid := Mux(curr_status===sMASTER1, io.slave1.arvalid, Mux(curr_status===sMASTER2, io.slave2.arvalid, 0.B))
    io.master1.araddr  := Mux(curr_status===sMASTER1, io.slave1.araddr,  Mux(curr_status===sMASTER2, io.slave2.araddr,  0.U))
    io.master1.arid    := Mux(curr_status===sMASTER1, io.slave1.arid,    Mux(curr_status===sMASTER2, io.slave2.arid,    0.U))
    io.master1.arlen   := Mux(curr_status===sMASTER1, io.slave1.arlen,   Mux(curr_status===sMASTER2, io.slave2.arlen,   0.U))
    io.master1.arsize  := Mux(curr_status===sMASTER1, io.slave1.arsize,  Mux(curr_status===sMASTER2, io.slave2.arsize,  0.U))
    io.master1.arburst := Mux(curr_status===sMASTER1, io.slave1.arburst, Mux(curr_status===sMASTER2, io.slave2.arburst, 1.U))

    // Read data channel
    io.master1.rready  := Mux(curr_status===sMASTER1, io.slave1.rready,  Mux(curr_status===sMASTER2, io.slave2.rready,  0.B))

    // process response of slave1 to master1
    io.slave1.awready  := Mux(curr_status===sMASTER1, io.master1.awready, 0.B)
    // Write data channel
    io.slave1.wready   := Mux(curr_status===sMASTER1, io.master1.wready,  0.B)
    // Write response channel
    io.slave1.bvalid   := Mux(curr_status===sMASTER1, io.master1.bvalid,  0.B)
    io.slave1.bresp    := Mux(curr_status===sMASTER1, io.master1.bresp,   0.U)
    io.slave1.bid      := Mux(curr_status===sMASTER1, io.master1.bid,     0.U)
    // Read address channel
    io.slave1.arready  := Mux(curr_status===sMASTER1, io.master1.arready, 0.B)
    // Read data channel
    io.slave1.rvalid   := Mux(curr_status===sMASTER1, io.master1.rvalid,  0.B)
    io.slave1.rdata    := Mux(curr_status===sMASTER1, io.master1.rdata,   0.U)
    io.slave1.rresp    := Mux(curr_status===sMASTER1, io.master1.rresp,   0.U)
    io.slave1.rlast    := Mux(curr_status===sMASTER1, io.master1.rlast,   1.B)
    io.slave1.rid      := Mux(curr_status===sMASTER1, io.master1.rid,     0.U)

    // process response of slave1 to master2
    // Write address channel
    io.slave2.awready  := Mux(curr_status===sMASTER2, io.master1.awready, 0.B)
    // Write data channel
    io.slave2.wready   := Mux(curr_status===sMASTER2, io.master1.wready,  0.B)
    // Write response channel
    io.slave2.bvalid   := Mux(curr_status===sMASTER2, io.master1.bvalid,  0.B)
    io.slave2.bresp    := Mux(curr_status===sMASTER2, io.master1.bresp,   0.U)
    io.slave2.bid      := Mux(curr_status===sMASTER2, io.master1.bid,     0.U)
    // Read address channel
    io.slave2.arready  := Mux(curr_status===sMASTER2, io.master1.arready, 0.B)
    // Read data channel
    io.slave2.rvalid   := Mux(curr_status===sMASTER2, io.master1.rvalid,  0.B)
    io.slave2.rdata    := Mux(curr_status===sMASTER2, io.master1.rdata,   0.U)
    io.slave2.rresp    := Mux(curr_status===sMASTER2, io.master1.rresp,   0.U)
    io.slave2.rlast    := Mux(curr_status===sMASTER2, io.master1.rlast,   1.B)
    io.slave2.rid      := Mux(curr_status===sMASTER2, io.master1.rid,     0.U)
  }
}

