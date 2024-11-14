import chisel3._
import chisel3.util._
import Control._
import Instructions._
import chisel3.util.MuxLookup

class CSRIO(dataWidth: Int) extends Bundle {
  val inst     = Input(UInt(dataWidth.W))
  val pc       = Input(UInt(dataWidth.W))
  val csr_cmd  = Input(UInt(3.W))
  val csr_data = Input(UInt(dataWidth.W))
  val csr_out  = Output(UInt(dataWidth.W))
  val valid    = Input(Bool())
}

class CSR(val dataWidth: Int) extends Module {
  val io = IO(new CSRIO(dataWidth))

  val csr_addr = Wire(UInt(12.W))
  val out      = Wire(UInt(dataWidth.W))

  val mtvec_en   = Wire(Bool())
  val mstatus_en = Wire(Bool())
  val mepc_en    = Wire(Bool())
  val mcause_en  = Wire(Bool())

  val mtvec   = RegInit(0.U(dataWidth.W))
  val mstatus = RegInit("h1800".U(dataWidth.W))
  val mepc    = RegInit(0.U(dataWidth.W))
  val mcause  = RegInit(0.U(dataWidth.W))
  val i       = RegInit(0.U(dataWidth.W))

  csr_addr := io.inst(31, 20)

  mtvec   := Mux(mtvec_en, Mux(io.csr_cmd === CSR_W, io.csr_data, (mtvec | io.csr_data)), mtvec)
  mstatus := Mux(mstatus_en, Mux(io.csr_cmd === CSR_W, io.csr_data, (mstatus | io.csr_data)), mstatus)
  mepc    := Mux(mepc_en, Mux(io.inst === ECALL, io.pc, Mux(io.csr_cmd === CSR_W, io.csr_data, (mepc | io.csr_data))), mepc)
  mcause  := Mux(mcause_en, Mux(io.inst === ECALL, "d11".U, Mux(io.csr_cmd === CSR_W, io.csr_data, (mcause | io.csr_data))), mcause)

  mtvec_en   := Mux(io.csr_cmd === CSR_N, 0.B, Mux(io.inst === ECALL, 0.B, Mux(io.inst === MRET, 0.B, Mux(csr_addr === "h305".U, 1.B, 0.B))))
  mstatus_en := Mux(io.csr_cmd === CSR_N, 0.B, Mux(io.inst === ECALL, 0.B, Mux(io.inst === MRET, 0.B, Mux(csr_addr === "h300".U, 1.B, 0.B))))
  mepc_en    := Mux(io.csr_cmd === CSR_N, 0.B, Mux(io.inst === ECALL, 1.B, Mux(io.inst === MRET, 0.B, Mux(csr_addr === "h341".U, 1.B, 0.B))))
  mcause_en  := Mux(io.csr_cmd === CSR_N, 0.B, Mux(io.inst === ECALL, 1.B, Mux(io.inst === MRET, 0.B, Mux(csr_addr === "h342".U, 1.B, 0.B))))

  out := MuxLookup(csr_addr, 0.U)(
    Seq("h305".U(12.W) -> mtvec,
        "h300".U(12.W) -> mstatus,
        "h341".U(12.W) -> mepc,
        "h342".U(12.W) -> mcause,
      ))

  when(io.inst === ECALL & io.valid) {
    // i := i + "h1".U(dataWidth.W)
    printf(p"ecall at 0x${Hexadecimal(io.pc)}  to 0x${Hexadecimal(mtvec)}\n")
    printf(p"      mepc = 0x${Hexadecimal(mepc)}\n")
  }
  when(io.inst === MRET & io.valid) {
    printf(p"mret at 0x${Hexadecimal(io.pc)}  to 0x${Hexadecimal(mepc)}\n")
    printf(p"     mtvec = 0x${Hexadecimal(mtvec)}\n")
  }
  /* when(io.inst =/= ECALL) {
    i := 0.U(dataWidth.W)
  } */
  when(io.inst === ECALL & io.valid =/= 1.B) {
    mepc_en := 0.B
  }

  io.csr_out := Mux(io.csr_cmd =/= CSR_P, out, Mux(io.inst === ECALL, mtvec, Mux(io.inst === MRET, mepc, out)))
}
