// 包含Verilator提供的verilated.h头文件，这是进行Verilog模拟所必需的
#include <verilated.h>
#include <verilated_vcd_c.h>
// 包含由Verilator从Verilog代码生成的C++头文件，通常命名为Vtop.h，它包含了模拟所需的类定义
#include "Vtop.h"
// INCLUDE MODULE CLASSES
#include "Vtop___024root.h"


#include "common.h"
//#include "load_img.cpp"
//int parse_args(int argc, char *argv[]);
//long load_img();


//模拟时间
vluint64_t sim_time = 0;

// 创建一个Vtop类的实例，代表被模拟的Verilog模块
Vtop *top = new Vtop;

VerilatedVcdC *m_trace = new VerilatedVcdC;

//ebreak
  extern "C" void ebreak(){
    printf("excute the ebreak inst\n");
    // 执行模型的最终清理工作
    top->final();
    m_trace->close();
    exit(0);
  }

char *img_file = NULL;
char *log_file = NULL;
char *diff_so_file = NULL;
char *elf_file = NULL;
//uint8_t buffer[1024];

NPCState npc_state;

CPU_state cpu;

static int difftest_port = 1234;

int main(int argc, char *argv[]) {

    //static char *img_file = NULL;
    parse_args(argc, argv);

    #ifdef CONFIG_FTRACE
    parse_elf(elf_file);
    #endif

    init_mem();

    //static char *img_file = NULL;
    long img_size = load_img();

    cpu.pc = 0x80000000;
    for(int x = 0  ; x < 32 ; x ++) {
        cpu.gpr[x] = top->rootp->top__DOT__ifu__DOT__rf__DOT___GEN[x];
    }
    
    #ifdef CONFIG_DIFFTEST
    init_difftest(diff_so_file, img_size, difftest_port);
    #endif

    #ifdef CONFIG_DEVICE
    init_device();
    #endif

    init_sdb();

    //iringbuf
    init_disasm("riscv32" "-pc-linux-gnu");

    welcome();

    // 防止编译器对未使用的变量argc和argv发出警告
    if (false && argc && argv) {}

    /* //生成波形
    Verilated::traceEverOn(true);
    top->trace(m_trace, 5);
    m_trace->open("waveform.vcd"); */

    // 设置模拟开始时的输入信号状态
    top->reset = 0;
    top->clock = 0;
    static int i = 0;

    // 进入模拟循环，直到模拟结束
    while (i<1) {//!contextp->gotFinish()

        sdb_mainloop();

        /*// 切换时钟信号
        top->clk = !top->clk;

        // 在时钟信号的上升沿改变控制信号
        /*if (top->clk) {
            if (contextp->time() > 1 && contextp->time() < 10) {
                top->reset = !1;  // Assert reset
            } else {
                top->reset = !0;  // Deassert reset
            }
            // Assign some other inputs
            
        }

        // 评估模拟模型
        top->eval();

        // 增加模拟时间
        m_trace->dump(sim_time);
        sim_time++;

        // 打印模拟过程中的信号状态
        printf("i = %d clk=%x reset=%x",
                  i, top->clk, top->reset);*/
                  i++;
    }

    // 执行模型的最终清理工作
    top->final();
    m_trace->close();

    return 0;
}
