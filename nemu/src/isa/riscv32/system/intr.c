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

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
   //NO对应异常种类，epc对应触发异常的指令地址，最后返回异常入口地址
  /*//0是自陷
  if(NO==0){
    epc+=4;
  }*/
  cpu.mcause = NO;//系统调用号
  cpu.mepc = epc;//把当前指令地址保存到 sepc 寄存器
  cpu.mstatus &= ~(1<<7);
  cpu.mstatus |= ((cpu.mstatus&(1<<3))<<4); // MPIE = MIE
  cpu.mstatus &= ~(1<<3); // MIE = 0
  cpu.mstatus |= ((1<<11)+(1<<12)); // MPP = 011 (m-mode)
  return cpu.mtvec;//跳转到 stvec 寄存器指向的地址
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
