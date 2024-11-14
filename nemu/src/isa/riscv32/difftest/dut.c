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

#include <isa.h>
#include <cpu/difftest.h>
#include "../local-include/reg.h"

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  int s = 1;
  int i = 0;
  for(; i < 32; i++)
  {
    if(cpu.gpr[i] != ref_r->gpr[i])
    {
      s = 0;
      printf("different reg\n");
      break;
    }
  }
  if(cpu.mstatus != ref_r->mstatus)
  {
    printf("different mstatus\n");
    printf("cpu: 0x%08x\n", cpu.mstatus);
    printf("ref: 0x%08x\n", ref_r->mstatus);
    s = 0;
  }
  if(cpu.mcause != ref_r->mcause)
  {
    printf("different mcause\n");
    printf("cpu: %d\n", cpu.mcause);
    printf("ref: %d\n", ref_r->mcause);
    printf("%d\n", cpu.gpr[17]);
    s = 0;
  }
  if(cpu.mepc != ref_r->mepc)
  {
    printf("different mepc\n");
    s = 0;
  }
  if(cpu.mtvec != ref_r->mtvec)
  {
    printf("different mtvec\n");
    s = 0;
  }
  if(s && cpu.pc == ref_r->pc)
  {
    return true;
  }
  pc = ref_r->pc;//pc应指向导致对比结果不一致的指令
  return false;
}

void isa_difftest_attach() {
}
