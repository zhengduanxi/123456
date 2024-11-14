import chisel3._
import chisel3.util._

class CLINT(xlen:Int) extends Module {
  val io = IO(new Bundle {
    val ACLK    = Input(Clock())
    val ARESETn = Input(Reset())

    // CLINT <=> Slave interface
    val sc      = new MessageMM(xlen)
  })

  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val rdata  = RegInit(0.U(xlen.W))
    val rvalid = RegInit(0.B)

    val mtime  = RegInit(0.U(64.W))

    // device register
    mtime  := mtime + 1.U

    // read mtime
    rdata  := Mux(io.sc.ren, Mux(io.sc.raddr==="ha0000048".U(xlen.W), mtime(31, 0),
                             Mux(io.sc.raddr==="ha000004c".U(xlen.W), mtime(63, 32), 0.U)), 0.U)
    rvalid := Mux(io.sc.ren, 1.B, 0.B)

    io.sc.rdata  := rdata
    io.sc.rvalid := rvalid

    // not write
    io.sc.wdone  := 0.B
  }
}
