import chisel3._
import Instructions._

object Control {
  val Y = true.B
  val N = false.B

  // PC_sel
  val PC_4   = 0.U(2.W)
  val PC_ALU = 1.U(2.W)
  val PC_CSR = 2.U(2.W)

  // RigsterFile
  val WB_SEL_PC_4 = 0.U(2.W)
  val WB_SEL_ALU  = 1.U(2.W)
  val WB_SEL_MEM  = 2.U(2.W)
  val WB_SEL_CSR  = 3.U(2.W)

  // Alu
  val A_SEL_XXX = 0.U(1.W)
  val A_SEL_RS1 = 0.U(1.W)
  val A_SEL_PC = 1.U(1.W)

  val B_SEL_XXX = 0.U(1.W)
  val B_SEL_RS2 = 0.U(1.W)
  val B_SEL_I = 1.U(1.W)

  val ALU_XXX  = 0.U(4.W)
  val ALU_ADD  = 0.U(4.W)
  val ALU_CP_A = 1.U(4.W)
  val ALU_CP_B = 2.U(4.W)
  val ALU_SLTU = 3.U(4.W)
  val ALU_SUB  = 4.U(4.W)
  val ALU_XOR  = 5.U(4.W)
  val ALU_SRA  = 6.U(4.W)
  val ALU_AND  = 7.U(4.W)
  val ALU_SL   = 8.U(4.W)
  val ALU_OR   = 9.U(4.W)
  val ALU_SRL  = 10.U(4.W)
  val ALU_SLT  = 11.U(4.W)

  // Imm_sel
  val IMM_I = 0.U(3.W)
  val IMM_U = 1.U(3.W)
  val IMM_J = 2.U(3.W)
  val IMM_S = 3.U(3.W)
  val IMM_B = 4.U(3.W)
  val IMM_X = 5.U(3.W)

  // Memory
  val ST_XXX = 0.U(2.W)
  val ST_SB  = 1.U(2.W)
  val ST_SH  = 2.U(2.W)
  val ST_SW  = 3.U(2.W)

  val LD_XXX = 0.U(3.W)
  val LD_LB  = 1.U(3.W)
  val LD_LH  = 2.U(3.W)
  val LD_LW  = 3.U(3.W)
  val LD_LBU = 4.U(3.W)
  val LD_LHU = 5.U(3.W)
  
  // Branch
  val BR_XXX  = 0.U(3.W)
  val BR_BNE  = 1.U(3.W)
  val BR_BEQ  = 2.U(3.W)
  val BR_BGE  = 3.U(3.W)
  val BR_BGEU = 4.U(3.W)
  val BR_BLTU = 5.U(3.W)
  val BR_BLT  = 6.U(3.W)

  // CSR
  val CSR_N = 0.U(3.W)
  val CSR_W = 1.U(3.W)
  val CSR_S = 2.U(3.W)
  val CSR_C = 3.U(3.W)
  val CSR_P = 4.U(3.W)


  val default = 
             List(PC_4,   IMM_X, ALU_XXX,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_XXX, B_SEL_XXX, WB_SEL_ALU,  CSR_N,N)
  val map = Array(
    LUI   -> List(PC_4,   IMM_U, ALU_CP_B, ST_XXX, LD_XXX, BR_XXX,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    AUIPC -> List(PC_4,   IMM_U, ALU_ADD,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    JAL   -> List(PC_ALU, IMM_J, ALU_ADD,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_PC,  B_SEL_I,   WB_SEL_PC_4, CSR_N,Y),
    JALR  -> List(PC_ALU, IMM_I, ALU_ADD,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_PC_4, CSR_N,Y),
    BEQ   -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BEQ,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    BNE   -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BNE,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    BLT   -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BLT,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    BGE   -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BGE,  A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    BLTU  -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BLTU, A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    BGEU  -> List(PC_4,   IMM_B, ALU_ADD,  ST_XXX, LD_XXX, BR_BGEU, A_SEL_PC,  B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    LB    -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_LB,  BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_MEM,  CSR_N,Y),
    LH    -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_LH,  BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_MEM,  CSR_N,Y),
    LW    -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_LW,  BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_MEM,  CSR_N,Y),
    LBU   -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_LBU, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_MEM,  CSR_N,Y),
    LHU   -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_LHU, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_MEM,  CSR_N,Y),
    SB    -> List(PC_4,   IMM_S, ALU_ADD,  ST_SB,  LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    SH    -> List(PC_4,   IMM_S, ALU_ADD,  ST_SH,  LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    SW    -> List(PC_4,   IMM_S, ALU_ADD,  ST_SW,  LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,N),
    ADDI  -> List(PC_4,   IMM_I, ALU_ADD,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    SLTI  -> List(PC_4,   IMM_I, ALU_SLT,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    SLTIU -> List(PC_4,   IMM_I, ALU_SLTU, ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    XORI  -> List(PC_4,   IMM_I, ALU_XOR,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    ORI   -> List(PC_4,   IMM_I, ALU_OR,   ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    ANDI  -> List(PC_4,   IMM_I, ALU_AND,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    SLLI  -> List(PC_4,   IMM_I, ALU_SL,   ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    SRLI  -> List(PC_4,   IMM_I, ALU_SRL,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    SRAI  -> List(PC_4,   IMM_I, ALU_SRA,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_I,   WB_SEL_ALU,  CSR_N,Y),
    ADD   -> List(PC_4,   IMM_X, ALU_ADD,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SUB   -> List(PC_4,   IMM_X, ALU_SUB,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SLL   -> List(PC_4,   IMM_X, ALU_SL,   ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SLT   -> List(PC_4,   IMM_X, ALU_SLT,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SLTU  -> List(PC_4,   IMM_X, ALU_SLTU, ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    XOR   -> List(PC_4,   IMM_X, ALU_XOR,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SRL   -> List(PC_4,   IMM_X, ALU_SRL,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    SRA   -> List(PC_4,   IMM_X, ALU_SRA,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    OR    -> List(PC_4,   IMM_X, ALU_OR,   ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    AND   -> List(PC_4,   IMM_X, ALU_AND,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_RS2, WB_SEL_ALU,  CSR_N,Y),
    CSRRW -> List(PC_4,   IMM_X, ALU_CP_A, ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_XXX, WB_SEL_CSR,  CSR_W,Y),
    CSRRS -> List(PC_4,   IMM_X, ALU_CP_A, ST_XXX, LD_XXX, BR_XXX,  A_SEL_RS1, B_SEL_XXX, WB_SEL_CSR,  CSR_S,Y),
    ECALL -> List(PC_CSR, IMM_X, ALU_XXX,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_XXX, B_SEL_XXX, WB_SEL_CSR,  CSR_P,N),
    MRET  -> List(PC_CSR, IMM_X, ALU_XXX,  ST_XXX, LD_XXX, BR_XXX,  A_SEL_XXX, B_SEL_XXX, WB_SEL_CSR,  CSR_P,N)
  )
}
