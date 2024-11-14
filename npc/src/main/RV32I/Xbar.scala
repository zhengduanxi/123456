import chisel3._
import chisel3.util._

class Xbar(xlen:Int) extends Module {
  val io = IO(new Bundle {
    val ACLK       = Input(Clock())
    val ARESETn    = Input(Reset())

    // Arbiter <=> Xbar
    val ax         = Flipped(new MessageMA(xlen))
    // Xbar <=> DSRAM
    val xbar_dsram = new MessageMA(xlen)
    // Xbar <=> UART
    val xbar_uart  = new MessageMA(xlen)
    // Xbar <=> CLINT
    val xbar_clint = new MessageMA(xlen)
  })

  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val dsram_done = Wire(Bool())
    val uart_done  = Wire(Bool())
    val clint_done = Wire(Bool())

    dsram_done := (io.xbar_dsram.BVALID && io.xbar_dsram.BREADY) || (io.xbar_dsram.RVALID && io.xbar_dsram.RREADY)
    uart_done := (io.xbar_uart.BVALID && io.xbar_uart.BREADY) || (io.xbar_uart.RVALID && io.xbar_uart.RREADY)
    clint_done := (io.xbar_clint.BVALID && io.xbar_clint.BREADY) || (io.xbar_clint.RVALID && io.xbar_clint.RREADY)

    // states
    val sIDEL :: sDSRAM :: sUART :: sCLINT :: sERROR :: Nil = Enum(5)

    val curr_status = RegInit(sIDEL)
    val next_status = WireDefault(sIDEL)

    // status transfer
    curr_status := next_status
    switch(curr_status) {
      is(sIDEL) {
        when (io.ax.AWVALID && io.ax.AWADDR >= "h1000_0000".U(xlen.W) && io.ax.AWADDR <= "h1000_0fff".U(xlen.W)) {
          next_status := sDSRAM
        } .elsewhen (io.ax.ARVALID && io.ax.ARADDR >= "h1000_0000".U(xlen.W) && io.ax.ARADDR <= "h1000_0fff".U(xlen.W)) {
          next_status := sDSRAM   // TODO: UART does not implement read function
        } .elsewhen (io.ax.AWVALID && io.ax.AWADDR >= "h8000_0000".U(xlen.W) && io.ax.AWADDR <= "h80ff_ffff".U(xlen.W)) {
          next_status := sDSRAM
        } .elsewhen (io.ax.ARVALID && io.ax.ARADDR >= "h8000_0000".U(xlen.W) && io.ax.ARADDR <= "h80ff_ffff".U(xlen.W)) {
          next_status := sDSRAM
        } .elsewhen (io.ax.AWVALID && io.ax.AWADDR >= "ha000_0048".U(xlen.W) && io.ax.AWADDR <= "ha000_004c".U(xlen.W)) {
          next_status := sDSRAM  // TODO: CLINT does not implement write function
        } .elsewhen (io.ax.ARVALID && io.ax.ARADDR >= "ha000_0048".U(xlen.W) && io.ax.ARADDR <= "ha000_004c".U(xlen.W)) {
          next_status := sDSRAM
        } .otherwise {
          next_status := sDSRAM
        }
      }

      is(sDSRAM) {
        when (dsram_done) {
          next_status := sIDEL
        } .otherwise {
          next_status := sDSRAM
        }
      }

      is(sUART) {
        when (uart_done) {
          next_status := sIDEL
        } .otherwise {
          next_status := sUART
        }
      }

      is(sCLINT) {
        when (clint_done) {
          next_status := sIDEL
        } .otherwise {
          next_status := sCLINT
        }
      }

      is (sERROR) {
        next_status := sIDEL
      }
    }

    // Xbar => UART
    // Write address channel
    io.xbar_uart.AWVALID := Mux(curr_status===sUART, io.ax.AWVALID, 0.B)
    io.xbar_uart.AWADDR  := Mux(curr_status===sUART, io.ax.AWADDR,  0.U)
    // io.xbar_uart.AWPROT  := Mux(curr_status===sUART, io.ax.AWPROT,  0.U)
    io.xbar_uart.AWID    := Mux(curr_status===sUART, io.ax.AWID,    0.U)
    io.xbar_uart.AWLEN   := Mux(curr_status===sUART, io.ax.AWLEN,   0.U)
    io.xbar_uart.AWSIZE  := Mux(curr_status===sUART, io.ax.AWSIZE,  0.U)
    io.xbar_uart.AWBURST := Mux(curr_status===sUART, io.ax.AWBURST, 0.U)
    // Write data channel
    io.xbar_uart.WVALID  := Mux(curr_status===sUART, io.ax.WVALID,  0.B)
    io.xbar_uart.WDATA   := Mux(curr_status===sUART, io.ax.WDATA,   0.U)
    io.xbar_uart.WSTRB   := Mux(curr_status===sUART, io.ax.WSTRB,   0.U)
    io.xbar_uart.WLAST   := Mux(curr_status===sUART, io.ax.WLAST,   0.B)
    // Write response channel
    io.xbar_uart.BREADY  := Mux(curr_status===sUART, io.ax.BREADY,  0.B)
    // Read address channel
    io.xbar_uart.ARVALID := Mux(curr_status===sUART, io.ax.ARVALID, 0.B)
    io.xbar_uart.ARADDR  := Mux(curr_status===sUART, io.ax.ARADDR,  0.U)
    // io.xbar_uart.ARPROT  := Mux(curr_status===sUART, io.ax.ARPROT,  0.U)
    io.xbar_uart.ARID    := Mux(curr_status===sUART, io.ax.ARID,    0.U)
    io.xbar_uart.ARLEN   := Mux(curr_status===sUART, io.ax.ARLEN,   0.U)
    io.xbar_uart.ARSIZE  := Mux(curr_status===sUART, io.ax.ARSIZE,  0.U)
    io.xbar_uart.ARBURST := Mux(curr_status===sUART, io.ax.ARBURST, 0.U)
    // Read data channel
    io.xbar_uart.RREADY  := Mux(curr_status===sUART, io.ax.RREADY,  0.B)

    // Xbar => DSRAM
    // Write address channel
    io.xbar_dsram.AWVALID := Mux(curr_status===sDSRAM, io.ax.AWVALID, 0.B)
    io.xbar_dsram.AWADDR  := Mux(curr_status===sDSRAM, io.ax.AWADDR,  0.U)
    // io.xbar_dsram.AWPROT  := Mux(curr_status===sDSRAM, io.ax.AWPROT,  0.U)
    io.xbar_dsram.AWID    := Mux(curr_status===sDSRAM, io.ax.AWID,    0.U)
    io.xbar_dsram.AWLEN   := Mux(curr_status===sDSRAM, io.ax.AWLEN,   0.U)
    io.xbar_dsram.AWSIZE  := Mux(curr_status===sDSRAM, io.ax.AWSIZE,  0.U)
    io.xbar_dsram.AWBURST := Mux(curr_status===sDSRAM, io.ax.AWBURST, 0.U)
    // Write data channel
    io.xbar_dsram.WVALID  := Mux(curr_status===sDSRAM, io.ax.WVALID,  0.B)
    io.xbar_dsram.WDATA   := Mux(curr_status===sDSRAM, io.ax.WDATA,   0.U)
    io.xbar_dsram.WSTRB   := Mux(curr_status===sDSRAM, io.ax.WSTRB,   0.U)
    io.xbar_dsram.WLAST   := Mux(curr_status===sDSRAM, io.ax.WLAST,   0.B)
    // Write response channel
    io.xbar_dsram.BREADY  := Mux(curr_status===sDSRAM, io.ax.BREADY,  0.B)
    // Read address channel
    io.xbar_dsram.ARVALID := Mux(curr_status===sDSRAM, io.ax.ARVALID, 0.B)
    io.xbar_dsram.ARADDR  := Mux(curr_status===sDSRAM, io.ax.ARADDR,  0.U)
    // io.xbar_dsram.ARPROT  := Mux(curr_status===sDSRAM, io.ax.ARPROT,  0.U)
    io.xbar_dsram.ARID    := Mux(curr_status===sDSRAM, io.ax.ARID,    0.U)
    io.xbar_dsram.ARLEN   := Mux(curr_status===sDSRAM, io.ax.ARLEN,   0.U)
    io.xbar_dsram.ARSIZE  := Mux(curr_status===sDSRAM, io.ax.ARSIZE,  0.U)
    io.xbar_dsram.ARBURST := Mux(curr_status===sDSRAM, io.ax.ARBURST, 0.U)
    // Read data channel
    io.xbar_dsram.RREADY  := Mux(curr_status===sDSRAM, io.ax.RREADY,  0.B)

    // Xbar => CLINT
    // Write address channel
    io.xbar_clint.AWVALID := Mux(curr_status===sCLINT, io.ax.AWVALID, 0.B)
    io.xbar_clint.AWADDR  := Mux(curr_status===sCLINT, io.ax.AWADDR,  0.U)
    // io.xbar_clint.AWPROT  := Mux(curr_status===sCLINT, io.ax.AWPROT,  0.U)
    io.xbar_clint.AWID    := Mux(curr_status===sCLINT, io.ax.AWID,    0.U)
    io.xbar_clint.AWLEN   := Mux(curr_status===sCLINT, io.ax.AWLEN,   0.U)
    io.xbar_clint.AWSIZE  := Mux(curr_status===sCLINT, io.ax.AWSIZE,  0.U)
    io.xbar_clint.AWBURST := Mux(curr_status===sCLINT, io.ax.AWBURST, 0.U)
    // Write data channel
    io.xbar_clint.WVALID  := Mux(curr_status===sCLINT, io.ax.WVALID,  0.B)
    io.xbar_clint.WDATA   := Mux(curr_status===sCLINT, io.ax.WDATA,   0.U)
    io.xbar_clint.WSTRB   := Mux(curr_status===sCLINT, io.ax.WSTRB,   0.U)
    io.xbar_clint.WLAST   := Mux(curr_status===sCLINT, io.ax.WLAST,   0.B)
    // Write response channel
    io.xbar_clint.BREADY  := Mux(curr_status===sCLINT, io.ax.BREADY,  0.B)
    // Read address channel
    io.xbar_clint.ARVALID := Mux(curr_status===sCLINT, io.ax.ARVALID, 0.B)
    io.xbar_clint.ARADDR  := Mux(curr_status===sCLINT, io.ax.ARADDR,  0.U)
    // io.xbar_clint.ARPROT  := Mux(curr_status===sCLINT, io.ax.ARPROT,  0.U)
    io.xbar_clint.ARID    := Mux(curr_status===sCLINT, io.ax.ARID,    0.U)
    io.xbar_clint.ARLEN   := Mux(curr_status===sCLINT, io.ax.ARLEN,   0.U)
    io.xbar_clint.ARSIZE  := Mux(curr_status===sCLINT, io.ax.ARSIZE,  0.U)
    io.xbar_clint.ARBURST := Mux(curr_status===sCLINT, io.ax.ARBURST, 0.U)
    // Read data channel
    io.xbar_clint.RREADY  := Mux(curr_status===sCLINT, io.ax.RREADY,  0.B)

    // slaves(UART , DSRAM and CLINT) => Xbar
    // Write address channel
    io.ax.AWREADY := Mux(curr_status===sUART,  io.xbar_uart.AWREADY,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.AWREADY,
                     Mux(curr_status===sCLINT, io.xbar_clint.AWREADY, 0.B)))
    // Write data channel
    io.ax.WREADY  := Mux(curr_status===sUART,  io.xbar_uart.WREADY,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.WREADY,
                     Mux(curr_status===sCLINT, io.xbar_clint.WREADY,  0.B)))
    // Write response channel
    io.ax.BVALID  := Mux(curr_status===sUART,  io.xbar_uart.BVALID,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.BVALID,
                     Mux(curr_status===sCLINT, io.xbar_clint.BVALID,  0.B)))
    io.ax.BRESP   := Mux(curr_status===sUART,  io.xbar_uart.BRESP,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.BRESP,
                     Mux(curr_status===sCLINT, io.xbar_clint.BRESP,   0.U)))
    io.ax.BID     := Mux(curr_status===sUART,  io.xbar_uart.BID,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.BID,
                     Mux(curr_status===sCLINT, io.xbar_clint.BID,     0.U)))
    // Read address channel
    io.ax.ARREADY := Mux(curr_status===sUART,  io.xbar_uart.ARREADY,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.ARREADY,
                     Mux(curr_status===sCLINT, io.xbar_clint.ARREADY, 0.B)))
    // Read data channel
    io.ax.RVALID  := Mux(curr_status===sUART,  io.xbar_uart.RVALID,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.RVALID,
                     Mux(curr_status===sCLINT, io.xbar_clint.RVALID,  0.B)))
    io.ax.RDATA   := Mux(curr_status===sUART,  io.xbar_uart.RDATA,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.RDATA,
                     Mux(curr_status===sCLINT, io.xbar_clint.RDATA,   0.U)))
    io.ax.RRESP   := Mux(curr_status===sUART,  io.xbar_uart.RRESP,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.RRESP,
                     Mux(curr_status===sCLINT, io.xbar_clint.RRESP,   0.U)))
    io.ax.RLAST   := Mux(curr_status===sUART,  io.xbar_uart.RLAST,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.RLAST,
                     Mux(curr_status===sCLINT, io.xbar_clint.RLAST,   0.B)))
    io.ax.RID     := Mux(curr_status===sUART,  io.xbar_uart.RID,
                     Mux(curr_status===sDSRAM, io.xbar_dsram.RID,
                     Mux(curr_status===sCLINT, io.xbar_clint.RID,     0.U)))
  }
}
