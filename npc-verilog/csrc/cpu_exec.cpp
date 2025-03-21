// 包含Verilator提供的verilated.h头文件，这是进行Verilog模拟所必需的
#include <verilated.h>
#include <verilated_vcd_c.h>
// 包含由Verilator从Verilog代码生成的C++头文件，通常命名为Vtop.h，它包含了模拟所需的类定义
#include "Vtop.h"
// INCLUDE MODULE CLASSES
#include "Vtop___024root.h"

#include <cstdio>
#include "common.h"
#include "log.h"

extern vluint64_t sim_time;
extern Vtop *top;
extern VerilatedVcdC *m_trace;

extern NPCState npc_state;
extern CPU_state cpu;

void cpu_exec(uint32_t n){

  for(uint32_t i = 0; i < 2*n; i++){

  switch (npc_state.state) {
    case NPC_END: case NPC_ABORT:
      printf("Program execution has ended. To restart the program, exit NPC and run again.\n");
      return;
    default: npc_state.state = NPC_RUNNING;
  }

        // 切换时钟信号
        top->clk = !top->clk;

        // 在时钟信号的上升沿改变控制信号
        /*if (top->clk) {
            if (contextp->time() > 1 && contextp->time() < 10) {
                top->reset = !1;  // Assert reset
            } else {
                top->reset = !0;  // Deassert reset
            }
            // Assign some other inputs
            
        }*/

    for(int x = 0  ; x < 32 ; x ++) {
        cpu.gpr[x] = top->rootp->top__DOT__exu__DOT__rf__DOT__rf[x];
    }
    if (top->clk == 1){
        #ifdef CONFIG_DIFFTEST
        difftest_step(top->rootp->top__DOT__PC, cpu.pc);
        #endif

        #ifdef CONFIG_WATCHPOINT
        check_watchpoint();
        #endif
    }
        // 评估模拟模型
        top->eval();

        // 增加模拟时间
        m_trace->dump(sim_time);
        sim_time++;

        // 打印模拟过程中的信号状态
        /*printf("clk=%x reset=%x",
                  top->clk, top->reset);*/

      if (top->clk == 1){
        #ifdef CONFIG_DEVICE
        device_update();
        #endif
      }

        switch (npc_state.state) {
         case NPC_STOP: return;

         case NPC_END: case NPC_ABORT:
          Log("npc: %s at pc = 0x%08x",
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) :
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
          //case NPC_QUIT: statistic();
        }
    }
}
