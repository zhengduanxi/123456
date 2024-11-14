import chisel3._

// data: IFU => IDU
class MessageFD(xlen: Int) extends Bundle {
  val inst = Output(UInt(xlen.W))
  val pc   = Output(UInt(xlen.W))
  val wb_addr = Output(UInt(5.W))
  val rs1_data = Output(UInt(xlen.W))
  val rs2_data = Output(UInt(xlen.W))
  val r10 = Output(UInt(xlen.W))
}

// data: IDU => EXU
class MessageDE(xlen: Int) extends Bundle {
  val inst = Output(UInt(xlen.W))
  val pc   = Output(UInt(xlen.W))

  val PC_sel   = Output(UInt(2.W))
  val Imm_sel  = Output(UInt(3.W))
  val Alu_op   = Output(UInt(4.W))
  val st_type  = Output(UInt(2.W))
  val ld_type  = Output(UInt(3.W))
  val br_type  = Output(UInt(3.W))
  val A_sel    = Output(UInt(1.W))
  val B_sel    = Output(UInt(1.W))
  val wb_sel   = Output(UInt(2.W))
  val csr_cmd  = Output(UInt(3.W))
  val wb_en    = Output(Bool())

  val wb_addr = Output(UInt(5.W))
  val rs1_data = Output(UInt(xlen.W))
  val rs2_data = Output(UInt(xlen.W))
}

// data: EXU => MEU
class MessageEM(xlen: Int) extends Bundle {
  val inst = Output(UInt(xlen.W))
  val pc   = Output(UInt(xlen.W))

  val PC_sel  = Output(UInt(2.W))
  val st_type = Output(UInt(2.W))
  val ld_type = Output(UInt(3.W))
  val wb_sel  = Output(UInt(2.W))
  val csr_cmd = Output(UInt(3.W))
  val wb_en   = Output(Bool())
  val br_taken = Output(Bool())

  val Alu_out  = Output(UInt(xlen.W))
  val wb_addr = Output(UInt(5.W))
  val rs2_data = Output(UInt(xlen.W))
}

// data: MEU => WBU
class MessageMW(xlen: Int) extends Bundle {
  val inst = Output(UInt(xlen.W))
  val pc   = Output(UInt(xlen.W))

  val PC_sel  = Output(UInt(2.W))
  val wb_sel  = Output(UInt(2.W))
  val csr_cmd = Output(UInt(3.W))
  val wb_en   = Output(Bool())
  val br_taken = Output(Bool())

  val Alu_out = Output(UInt(xlen.W))
  val wb_addr = Output(UInt(5.W))
  val rd_data = Output(UInt(xlen.W))
}

// data: WBU => IFU
class MessageWI(xlen: Int) extends Bundle {
  val PC_sel  = Output(UInt(2.W))
  val wb_sel  = Output(UInt(2.W))
  val wb_en   = Output(Bool())
  val br_taken = Output(Bool())

  val csr_out = Output(UInt(xlen.W))
  val Alu_out = Output(UInt(xlen.W))
  val wb_addr = Output(UInt(5.W))
  val rd_data = Output(UInt(xlen.W))
}

// data: Master interface <=> Arbiter
class MessageMA(xlen: Int) extends Bundle {
  val AWVALID = Output(Bool())
  val AWREADY = Input(Bool())
  val AWADDR  = Output(UInt(xlen.W))
  // val AWPROT  = Output(UInt(3.W))
  val AWID    = Output(UInt(4.W))
  val AWLEN   = Output(UInt(8.W))
  val AWSIZE  = Output(UInt(3.W))
  val AWBURST = Output(UInt(2.W))

  // Write data channel
  val WVALID  = Output(Bool())
  val WREADY  = Input(Bool())
  val WDATA   = Output(UInt(xlen.W))
  val WSTRB   = Output(UInt((xlen/8).W))
  val WLAST   = Output(Bool())

  // Write response channel
  val BVALID  = Input(Bool())
  val BREADY  = Output(Bool())
  val BRESP   = Input(UInt(2.W))
  val BID    = Input(UInt(4.W))

  // Read address channel
  val ARVALID = Output(Bool())
  val ARREADY = Input(Bool())
  val ARADDR  = Output(UInt(xlen.W))
  // val ARPROT  = Output(UInt(3.W))
  val ARID    = Output(UInt(4.W))
  val ARLEN   = Output(UInt(8.W))
  val ARSIZE  = Output(UInt(3.W))
  val ARBURST = Output(UInt(2.W))

  // Read data channel
  val RVALID  = Input(Bool())
  val RREADY  = Output(Bool())
  val RDATA   = Input(UInt(xlen.W))
  val RRESP   = Input(UInt(2.W))
  val RLAST   = Input(Bool())
  val RID     = Input(UInt(4.W))
}

// data: Master interface <=> Master
class MessageMM(xlen: Int) extends Bundle {
  val ren    = Input(Bool())
  val raddr  = Input(UInt(xlen.W))
  // val arsize = Input(UInt(3.W))
  val rdata  = Output(UInt(xlen.W))
  val rvalid = Output(Bool())

  val wen    = Input(Bool())
  val waddr  = Input(UInt(xlen.W))
  val wdata  = Input(UInt(xlen.W))
  val wmask  = Input(UInt(8.W))
  // val awsize = Input(UInt(3.W))
  val wdone  = Output(Bool())
}
