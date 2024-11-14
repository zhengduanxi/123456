import chisel3._
import chisel3.util._

class UART(xlen:Int) extends Module {
  val io = IO(new Bundle {
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())

    // UART <=> Slave interface
    val su      = new MessageMM(xlen)
  })

  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val device_register = RegInit(0.U(8.W))
    val statu_register  = RegInit(0.B)
    val wdone           = RegInit(0.B)

    // device register
    device_register := Mux(io.su.wen, io.su.wdata(7, 0), device_register)
    // status register
    statu_register  := io.su.wen
    // output data (change verilog code)
    wdone           := Mux(statu_register, 1.B, 0.B)

    io.su.wdone  := wdone

    // not read
    io.su.rdata  := 0.U
    io.su.rvalid := 0.B
  }
}
