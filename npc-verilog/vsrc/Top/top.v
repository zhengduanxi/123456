`timescale 1ns / 1ps
module top(
    input               clk,
    input               reset,
    output              MemWrite,//存储器写信号输出
    output      [31:0]  DataAdr,//数据地址输出
    output      [31:0]  WriteData//写入数据输出
               
    );
    wire  PCSrc, Zero, ALUSrc, RegWrite, MemRead, Jump, Ret;
    wire  [2:0]  ResultSrc;
    wire  [4:0]  ALUControl;
    wire  [31:0] ImmExt, PC, Instr, ReadData, PCPlus4, PCTarget, PCNext, R10, len;
    reg   [31:0] ReadData1;

    /*always @(posedge clk) begin
    $display("PC = %08x", PC);
    $display("Instr = %08x", Instr);
    $display("Zero = %b", Zero);
    $display("ResultSrc = %b", ResultSrc);
    $display("MemWrite = %b", MemWrite);
    $display("MemRead = %b", MemRead);
    $display("PCSrc = %b", PCSrc);
    $display("ALUSrc = %b", ALUSrc);
    $display("RegWrite = %b", RegWrite);
    $display("Jump = %b", Jump);
    $display("ALUControl = %b", ALUControl);
    $display("ImmExt = %08x", ImmExt);
    $display("WriteData = %08x", WriteData);
    $display("ALUResult = %08x", DataAdr);
    $display("ReadData = %08x", ReadData);
    $display("PCNext = %08x", PCNext);
    end*/
    
    //ebreak
    import "DPI-C" function void ebreak();
    import "DPI-C" function void npctrap(input int PC, input int R10);
    always @(*) begin
        if(Instr == 32'h00100073) begin 
            npctrap(PC, R10);
            ebreak();
        end 
    end

    //iringbuf
    import "DPI-C" function void trace_inst(input int pc, input int inst);
    always @(posedge clk) begin
        trace_inst(PC, Instr);
    end

    //ftrace
    import "DPI-C" function void display_call_func(input int pc, input int func_addr);
    import "DPI-C" function void display_ret_func(input int pc);
    always @(*) begin
        if(Instr[6:0] == 7'b1101111 && Instr[11:7] == 1) begin 
            display_call_func(PC, PCTarget);
        end 
        else if(Instr[6:0] == 7'b1100111) begin
             if(Instr[11:7] == 1) begin 
                display_call_func(PC, PCTarget);
             end
             else if(Instr[11:7] == 0 && Instr[19:15] == 1) begin
                display_ret_func(PC);
             end
        end
    end

    import "DPI-C" function void get_cpu_pc(input int PCNext);
    always @(*) begin
        get_cpu_pc(PCNext);
    end

    always @(*) begin
        if(MemRead) begin case(Instr[14:12])
                  000: ReadData1 = {{24{ReadData[7]}}, ReadData[7:0]};//lb
                  001: ReadData1 = {{16{ReadData[15]}}, ReadData[15:0]};//lh
                  default: ReadData1 = ReadData;
                  endcase
        end
    end

    IFU          ifu(
    .clk         (clk),
    .reset       (reset),
    .PCSrc       (PCSrc),
    .ImmExt      (ImmExt),
    .Ret         (Ret),
    .ALUResult   (DataAdr),
    .PC          (PC),
    .PCPlus4     (PCPlus4),
    .PCTarget    (PCTarget),
    .PCNext      (PCNext)
    );
    IDU          idu(
    .PC          (PC),
    .Instr       (Instr),
    .Zero        (Zero),
    .ResultSrc   (ResultSrc),
    .MemWrite    (MemWrite),
    .MemRead     (MemRead),
    .PCSrc       (PCSrc), 
    .ALUSrc      (ALUSrc),
    .RegWrite    (RegWrite), 
    .Jump        (Jump),
    .ALUControl  (ALUControl),
    .ImmExt      (ImmExt),
    .Ret         (Ret),
    .len         (len)
    );
    EXU          exu(
    .clk         (clk),
    .Instr       (Instr),
    .RegWrite    (RegWrite),
    .ImmExt      (ImmExt),
    .ALUSrc      (ALUSrc),
    .ALUControl  (ALUControl),
    .ReadData    (ReadData1),
    .PCPlus4     (PCPlus4),
    .PCTarget    (PCTarget),
    .ResultSrc   (ResultSrc),
    .WriteData   (WriteData),
    .Zero        (Zero),
    .ALUResult   (DataAdr),
    .R10         (R10)
    );
    imem imem_u(
     .A                     (PC)        ,
     .RD                    (Instr)//存储指令（Instr）
    );
    dmem dmem_u(
    . clk                   (clk)       , 
    . we                    (MemWrite)  ,
    . valid                 (MemRead)   , 
    . a                     (DataAdr)   , 
    . wd                    (WriteData) , 
    . len                   (len)       ,
    . rd                    (ReadData)//存储读出的数据 （ReadData）
    );

endmodule
