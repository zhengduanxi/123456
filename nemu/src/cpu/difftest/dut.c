/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <dlfcn.h>

#include <isa.h>
#include <cpu/cpu.h>
#include <memory/paddr.h>
#include <utils.h>
#include <difftest-def.h>

void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;

#ifdef CONFIG_DIFFTEST

static bool is_skip_ref = false;
static int skip_dut_nr_inst = 0;

// this is used to let ref skip instructions which
// can not produce consistent behavior with NEMU
void difftest_skip_ref() {
  is_skip_ref = true;//当设置为 true 时，指示差异测试框架跳过当前指令的参考实现检查
  // If such an instruction is one of the instruction packing in QEMU
  // (see below), we end the process of catching up with QEMU's pc to
  // keep the consistent behavior in our best.
  // Note that this is still not perfect: if the packed instructions
  // already write some memory, and the incoming instruction in NEMU
  // will load that memory, we will encounter false negative. But such
  // situation is infrequent.
  skip_dut_nr_inst = 0;//用于记录需要跳过的DUT（Device Under Test，被测试设备）指令的数量。在这里，它被重置为0，意味着在跳过参考实现检查的同时，重置DUT的跳过计数
}

// this is used to deal with instruction packing in QEMU.
// Sometimes letting QEMU step once will execute multiple instructions.
// We should skip checking until NEMU's pc catches up with QEMU's pc.
// The semantic is
//   Let REF run `nr_ref` instructions first.
//   We expect that DUT will catch up with REF within `nr_dut` instructions.
void difftest_skip_dut(int nr_ref, int nr_dut) {//通过先行执行 nr_ref 条指令的参考实现，并设置DUT需要跳过的指令数量，可以确保在进行差异检查时，NEMU的PC与QEMU的PC保持一致
  skip_dut_nr_inst += nr_dut;//skip_dut_nr_inst 增加了 nr_dut，nr_dut 参数指定了DUT需要跳过的指令数量

  while ((nr_ref--) > 0) {//while 循环执行 nr_ref 条参考实现的指令，以期望 DUT 能在接下来的 nr_dut 条指令内赶上
    ref_difftest_exec(1);//每次调用 ref_difftest_exec(1) 来执行一条参考实现的指令
  }
}

void init_difftest(char *ref_so_file, long img_size, int port) {
  assert(ref_so_file != NULL);

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);//使用 dlopen 函数打开共享对象文件（.so 文件），获取文件句柄。RTLD_LAZY 标志表示符号解析将延迟到实际使用时
  assert(handle);

  ref_difftest_memcpy = dlsym(handle, "difftest_memcpy");//使用 dlsym 函数获取 difftest_memcpy 符号的地址，并将其存储在 ref_difftest_memcpy 变量中
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec = dlsym(handle, "difftest_exec");//获取 difftest_regcpy、difftest_exec 和 difftest_raise_intr 函数的地址
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = dlsym(handle, "difftest_raise_intr");//获取 difftest_init 函数的地址，并将其存储在一个函数指针变量中
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)(int) = dlsym(handle, "difftest_init");
  assert(ref_difftest_init);

  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);

  ref_difftest_init(port);//调用 ref_difftest_init 函数，传入 port 参数，以初始化参考实现
  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);//调用 ref_difftest_memcpy 函数，将镜像从访客（模拟器）地址空间复制到主机地址空间，并指定为差异测试的参考端
  ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);//调用 ref_difftest_regcpy 函数，将当前CPU状态的寄存器值复制到参考实现的寄存器状态中
}

static void checkregs(CPU_state *ref, vaddr_t pc) {
  if (!isa_difftest_checkregs(ref, pc)) {//如果 isa_difftest_checkregs 返回 false
    printf("difftest error\n");
    nemu_state.state = NEMU_ABORT;
    nemu_state.halt_pc = pc;
    isa_reg_display();
  }
}

void difftest_step(vaddr_t pc, vaddr_t npc) {
  CPU_state ref_r;//声明了一个 CPU_state 类型的变量 ref_r，用于存储参考实现的CPU状态

  if (skip_dut_nr_inst > 0) {//表示之前有指令跳过，需要进行同步检查
    ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);//从DUT复制寄存器状态到参考实现的 CPU_state 结构
    if (ref_r.pc == npc) {
      skip_dut_nr_inst = 0;//不用跳过指令了
      checkregs(&ref_r, npc);//调用 checkregs 函数比较寄存器状态
      return;
    }
    skip_dut_nr_inst --;//减少跳过的指令计数
    if (skip_dut_nr_inst == 0)//如果跳过的指令计数达到0，但参考实现的PC值与预期不符，调用 panic 函数打印错误消息并退出
      panic("can not catch up with ref.pc = " FMT_WORD " at pc = " FMT_WORD, ref_r.pc, pc);
    return;
  }

  if (is_skip_ref) {
    // to skip the checking of an instruction, just copy the reg state to reference design
    ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);//将DUT的寄存器状态复制到参考实现
    is_skip_ref = false;
    return;
  }

  ref_difftest_exec(1);//在参考实现中执行一条指令
  //printf("before ref pc:%08x\n",ref_r.pc);
  ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);//将参考实现执行后的寄存器状态复制到 ref_r。
  //printf("after  ref pc:%08x\n",ref_r.pc);
  checkregs(&ref_r, pc);//调用 checkregs 函数，传入参考实现的CPU状态和当前指令的PC值，以检查寄存器状态是否一致。
}
#else
void init_difftest(char *ref_so_file, long img_size, int port) { }
#endif
