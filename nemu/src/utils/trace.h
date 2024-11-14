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

#ifndef __TRACE_H__
#define __TRACE_H__

#include <common.h>
#include <device/map.h>

void trace_inst(word_t pc, uint32_t inst);
void display_inst();
void display_memory_read(paddr_t addr, int len);
void display_memory_write(paddr_t addr, int len, word_t data);
void display_call_func(word_t pc, word_t func_addr);
void display_ret_func(word_t pc);
void display_device_read(paddr_t addr, int len, IOMap *map);
void display_device_write(paddr_t addr, int len, word_t data, IOMap *map);
void display_ecall(word_t pc, word_t npc);
void display_mret(word_t pc, word_t npc);

#endif
