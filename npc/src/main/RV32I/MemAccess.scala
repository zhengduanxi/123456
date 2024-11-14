import chisel3._
import chisel3.util._
import Control._
import chisel3.util.MuxLookup

class MemAccessIO(dataWidth: Int, addrWidth: Int) extends Bundle {
  val ACLK      = Input(Clock())
  val ARESETn   = Input(Reset())

  /* Mem <=> Master interface */
  val mm        =  Flipped(new MessageMM(dataWidth))

  /* MEU <=> Mem */
  val meu_valid = Input(Bool())
  val addr      = Input(UInt(addrWidth.W))
  val st_type   = Input(UInt(2.W))
  val ld_type   = Input(UInt(3.W))
  val wr_data   = Input(UInt(dataWidth.W))
  val rd_data   = Output(UInt(dataWidth.W))
  val mem_valid = Output(Bool())
}

// class Mem_read(dataWidth: Int, addrWidth: Int) extends BlackBox {
//   val io = IO(new Bundle {
//     val clk   = Input(Clock())
//     val valid = Input(UInt(1.W))
//     val wen   = Input(UInt(1.W))
//     val raddr = Input(UInt(addrWidth.W))
//     val rdata = Output(UInt(dataWidth.W))
//   })
// }

// class Mem_write(dataWidth:Int, addrWidth: Int) extends BlackBox {
//   val io = IO(new Bundle {
//     val clk   = Input(Clock())
//     val valid = Input(UInt(1.W))
//     val wen   = Input(UInt(1.W))
//     val waddr = Input(UInt(addrWidth.W))
//     val wdata = Input(UInt(dataWidth.W))
//     val wmask = Input(UInt(8.W))
//   })
// }

class MemAccess(val dataWidth: Int, val addrWidth: Int) extends Module {
  val io = IO(new MemAccessIO(dataWidth, addrWidth))

  val rdata = Wire(UInt(dataWidth.W))

  val wmask = Wire(UInt(8.W))
  val st_valid = io.st_type(1) | io.st_type(0)
  val ld_valid = io.ld_type(2) | io.ld_type(1) | io.ld_type(0)

  // val wen = st_valid

  // val mem_read = Module(new Mem_read(dataWidth, addrWidth))
  // val mem_write = Module(new Mem_write(dataWidth, addrWidth))

  rdata := io.mm.rdata
  io.mm.raddr  := io.addr
  io.mm.waddr  := io.addr
  io.mm.wdata  := io.wr_data
  io.mm.wmask  := wmask
  io.mem_valid := Mux(st_valid===1.U || ld_valid===1.U, io.mm.rvalid || io.mm.wdone, 1.B)


  io.rd_data := MuxLookup(Cat(io.addr(1, 0), io.ld_type), 0.S(32.W))(
    Seq(Cat(0.U(2.W), LD_LB)  -> rdata(7,  0).asSInt,
        Cat(1.U(2.W), LD_LB)  -> rdata(7,  0).asSInt,
        Cat(2.U(2.W), LD_LB)  -> rdata(7,  0).asSInt,
        Cat(3.U(2.W), LD_LB)  -> rdata(7,  0).asSInt,
        Cat(0.U(2.W), LD_LH)  -> rdata(15, 0).asSInt,
        Cat(2.U(2.W), LD_LH)  -> rdata(15, 0).asSInt,
        Cat(0.U(2.W), LD_LW)  -> rdata.asSInt,
        Cat(1.U(2.W), LD_LW)  -> rdata.asSInt,
        Cat(2.U(2.W), LD_LW)  -> rdata.asSInt,
        Cat(3.U(2.W), LD_LW)  -> rdata.asSInt,
        Cat(0.U(2.W), LD_LBU) -> Cat("h0".U, rdata(7,  0)).asSInt,
        Cat(1.U(2.W), LD_LBU) -> Cat("h0".U, rdata(7,  0)).asSInt,
        Cat(2.U(2.W), LD_LBU) -> Cat("h0".U, rdata(7,  0)).asSInt,
        Cat(3.U(2.W), LD_LBU) -> Cat("h0".U, rdata(7,  0)).asSInt,
        Cat(0.U(2.W), LD_LHU) -> Cat("h0".U, rdata(15, 0)).asSInt,
        Cat(2.U(2.W), LD_LHU) -> Cat("h0".U, rdata(15, 0)).asSInt
      )).asUInt

  wmask := MuxLookup(Cat(io.ld_type), 0.U(8.W))(
    Seq(Cat(LD_LB)   -> "b00000001".U,
        Cat(LD_LH)   -> "b00000010".U,
        Cat(LD_LW)   -> "b00000100".U,
        Cat(LD_LBU)  -> "b00000001".U,
        Cat(LD_LHU)  -> "b00000010".U,
      ))

  wmask := MuxLookup(Cat(io.addr(1, 0), io.st_type), 0.U(8.W))(
    Seq(Cat(0.U(2.W), ST_SB)  -> "b00000001".U,
        Cat(1.U(2.W), ST_SB)  -> "b00000010".U,
        Cat(2.U(2.W), ST_SB)  -> "b00000100".U,
        Cat(3.U(2.W), ST_SB)  -> "b00001000".U,
        Cat(0.U(2.W), ST_SH)  -> "b00000011".U,
        Cat(2.U(2.W), ST_SH)  -> "b00001100".U,
        Cat(0.U(2.W), ST_SW)  -> "b00001111".U,
      ))

  withClockAndReset(io.ACLK, !(io.ARESETn).asBool) {
    val ren = RegInit(0.B)
    val wen = RegInit(0.B)

    val sIDEL :: sREAD :: sWRITE :: sWAIT :: Nil = Enum(4)
    val curr_status = RegInit(sIDEL)
    val next_status = WireDefault(sIDEL)

    // status transfer
    curr_status := next_status
    switch (curr_status) {
      is (sIDEL) {
        when (io.meu_valid && ld_valid===1.U) {
          next_status := sREAD
        } .elsewhen (io.meu_valid && st_valid===1.U) {
          next_status := sWRITE
        } .otherwise {
          next_status := sIDEL
        }
      }

      is (sREAD) {
        next_status := sWAIT
      }

      is (sWRITE) {
        next_status := sWAIT
      }

      is (sWAIT) {
        when (!io.meu_valid) {
          next_status := sIDEL
        } .otherwise {
          next_status := sWAIT
        }
      }
    }

    // ren and wen
    ren := Mux(curr_status === sREAD,  1.B, 0.B)
    wen := Mux(curr_status === sWRITE, 1.B, 0.B)

    io.mm.ren := ren
    io.mm.wen := wen
  }
}
