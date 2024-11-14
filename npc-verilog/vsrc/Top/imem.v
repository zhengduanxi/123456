`timescale 1ns / 1ps
module imem(
    input       [31:0]      A,
    output      [31:0]      RD
    );
    /*// reg define
    reg    [7:0]  RAM[255:0];
    // initial �ǲ����ۺ���� ��������ֻ���з��� ����дû��ϵ
    initial begin
       $readmemh("/home/zhengduanxi/ysyx-workbench/npc/riscvtest.txt",RAM); 
    end
    assign RD = {RAM[A],RAM[A+1],RAM[A+2],RAM[A+3]}; // word aligned ��ַ����Ϊ4 ���A�������λ���Ժ���
//    assign RD = RAM[4]; // word aligned ��ַ����Ϊ4 ���A�������λ���Ժ���*/
import "DPI-C" function int pmem_read(input int addr, input int len);
//reg [31:0] rdata;
//always @(*) begin
    //rdata = pmem_read(A);
//end
assign RD = pmem_read(A, 4);
endmodule
