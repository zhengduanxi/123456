#ifndef __COMMON_H__
#define __COMMON_H__

#include "../include/generated/autoconf.h"
#include <cstdint>

int parse_args(int argc, char *argv[]);
long load_img();
extern "C" uint32_t pmem_read(uint32_t addr, int len);
void welcome();
void sdb_mainloop();
void cpu_exec(uint32_t n);
void isa_reg_display();
uint32_t isa_reg_str2val(const char *s, bool *success);
uint32_t expr(char *e, bool *success);
void init_regex();
void init_wp_pool();
void init_sdb();
void sdb_watchpoint_display();
void create_watchpoint(char* args);
void delete_watchpoint(int no);
void check_watchpoint();
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
extern "C" void init_disasm(const char *triple);
void display_inst();
void parse_elf(const char *elf_file);
void init_difftest(char *ref_so_file, long img_size, int port);
void init_mem();
uint8_t* guest_to_host(uint32_t paddr);
void difftest_step(uint32_t pc, uint32_t npc);
void difftest_skip_ref();
uint32_t host_read(void *addr, int len);
void host_write(void *addr, int len, uint32_t data);
bool in_pmem(uint32_t addr);
uint32_t mmio_read(uint32_t addr, int len);
void mmio_write(uint32_t addr, int len, uint32_t data);
void init_device();
void device_update();
extern "C" void display_memory_read(uint32_t addr, int len, uint32_t data);
extern "C" void display_memory_write(uint32_t addr, int len, uint32_t data);

enum { NPC_RUNNING, NPC_STOP, NPC_END, NPC_ABORT, NPC_QUIT };
typedef struct {
  int state;
  uint32_t halt_pc;
  uint32_t halt_ret;
} NPCState;

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

typedef struct {
  uint32_t gpr[32];
  uint32_t pc;
} CPU_state;

typedef void(*io_callback_t)(uint32_t, int, bool);
uint8_t* new_space(int size);
typedef struct {
  const char *name;
  // we treat ioaddr_t as uint32_t here
  uint32_t low;//映射的起始地址
  uint32_t high;//和结束地址
  void *space;//映射的目标空间
  io_callback_t callback;//回调函数
} IOMap;

void display_device_read(uint32_t addr, int len,  uint32_t data, IOMap *map);
void display_device_write(uint32_t addr, int len, uint32_t data, IOMap *map);

#endif
