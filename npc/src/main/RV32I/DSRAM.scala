/* import chisel3._
import chisel3.util._

class Mem_read(xlen:Int) extends BlackBox {
  val io = IO(new Bundle {
    val ACLK      = Input(Clock())
    val ARESETn   = Input(Reset())
    val ren       = Input(Bool())
    val raddr     = Input(UInt(xlen.W))
    val rdata     = Output(UInt(xlen.W))
    val wmask     = Input(UInt(8.W))
    val rvalid    = Output(Bool())
  })
}

class Mem_write(xlen:Int) extends BlackBox {
  val io = IO(new Bundle {
    val ACLK      = Input(Clock())
    val ARESETn   = Input(Reset())
    val wen       = Input(Bool())
    val waddr     = Input(UInt(xlen.W))
    val wdata     = Input(UInt(xlen.W))
    val wmask     = Input(UInt(8.W))
    val wdone     = Output(Bool())
  })
}

class DSRAM(xlen:Int) extends Module {
  val io = IO(new Bundle {
    val ACLK      = Input(Clock())
    val ARESETn   = Input(Reset())

    /* Slave <=> Slave interface */
    val ss        = new MessageMM(xlen)
  })

  val mem_read  = Module(new Mem_read(xlen))
  val mem_write = Module(new Mem_write(xlen))

  mem_read.io.ACLK    := io.ACLK
  mem_read.io.ARESETn := io.ARESETn
  mem_read.io.ren     := io.ss.ren
  mem_read.io.raddr   := io.ss.raddr
  mem_read.io.wmask   := io.ss.wmask

  mem_write.io.ACLK    := io.ACLK
  mem_write.io.ARESETn := io.ARESETn
  mem_write.io.wen     := io.ss.wen
  mem_write.io.waddr   := io.ss.waddr
  mem_write.io.wdata   := io.ss.wdata
  mem_write.io.wmask   := io.ss.wmask
  
  io.ss.rdata      := mem_read.io.rdata
  io.ss.rvalid     := mem_read.io.rvalid
  io.ss.wdone      := mem_write.io.wdone
}
 */