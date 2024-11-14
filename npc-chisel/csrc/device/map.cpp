//#include <isa.h>
//#include <memory/host.h>
//#include <memory/vaddr.h>
#include <cstdint>
#include <cstddef>
#include <cstdlib>
#include <cassert>
#include "map.h"

//#include "/home/zhengduanxi/ysyx-workbench/nemu/src/utils/trace.h"
//void display_device_read(paddr_t addr, int len, IOMap *map);
//void display_device_write(paddr_t addr, int len, word_t data, IOMap *map);

#define IO_SPACE_MAX (2 * 1024 * 1024)

static uint8_t *io_space = NULL;
static uint8_t *p_space = NULL;

uint8_t* new_space(int size) {
  uint8_t *p = p_space;
  // page aligned;   PAGE_SIZE 定义了页面的大小，PAGE_MASK 通常是一个掩码，用于清除对齐所需的最低位
  size = (size + (PAGE_SIZE - 1)) & ~PAGE_MASK;//通过计算确保分配的内存大小 size 是按页面大小对齐的
  p_space += size;//p_space 指针移动 size 字节，指向新分配内存空间之后的位置
  assert(p_space - io_space < IO_SPACE_MAX);
  return p;
}

extern CPU_state cpu;
static void check_bound(IOMap *map, uint32_t addr) {
  if (map == NULL) {
    printf("address (" FMT_PADDR ") is out of bound at pc = " FMT_WORD "\n", addr, cpu.pc-4);
    for(int i = 0; i < 32; i++){
      printf("reg%d   %08x\n", i, cpu.gpr[i]);
    }
    fflush(stdout);
    assert(map != NULL);
  } else {
    assert(addr <= map->high && addr >= map->low);
        /*"address (" FMT_PADDR ") is out of bound {%s} [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
        addr, map->name, map->low, map->high, cpu.pc);*/
  }
}

static void invoke_callback(io_callback_t c, uint32_t offset, int len, bool is_write) {
  if (c != NULL) { c(offset, len, is_write); }//使用回调函数
}

void init_map() {
  io_space = (uint8_t*)malloc(IO_SPACE_MAX);
  assert(io_space);
  p_space = io_space;
}

uint32_t map_read(uint32_t addr, int len, IOMap *map) {//map_read()和map_write()用于将地址addr映射到map所指示的目标空间, 并进行访问
  assert(len >= 1 && len <= 8);
  check_bound(map, addr);
  //IFDEF(CONFIG_DTRACE, display_device_read(addr, len, map));
  uint32_t offset = addr - map->low;
  invoke_callback(map->callback, offset, len, false); // prepare data to read
  uint32_t ret = host_read((uint8_t*)(map->space) + offset, len);
  #ifdef CONFIG_DTRACE
  display_device_read(addr, len, ret, map);
  #endif
  return ret;
}

void map_write(uint32_t addr, int len, uint32_t data, IOMap *map) {
  assert(len >= 1 && len <= 8);
  check_bound(map, addr);
  #ifdef CONFIG_DTRACE
  display_device_write(addr, len, data, map);
  #endif
  //IFDEF(CONFIG_DTRACE, display_device_write(addr, len, data, map));
  uint32_t offset = addr - map->low;//offset
  host_write((uint8_t*)(map->space) + offset, len, data);
  invoke_callback(map->callback, offset, len, true);//使用回调函数 map->callback(c(offset, len, is_write));
}
