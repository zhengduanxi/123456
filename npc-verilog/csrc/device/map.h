#ifndef __DEVICE_MAP_H__
#define __DEVICE_MAP_H__

//#include <cpu/difftest.h>
#include <cstdint>
#include "../common.h"
#include "../log.h"

uint64_t get_time();

typedef uint16_t ioaddr_t;
#define PAGE_SHIFT        12
#define PAGE_SIZE         (1ul << PAGE_SHIFT)
#define PAGE_MASK         (PAGE_SIZE - 1)

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

static inline bool map_inside(IOMap *map, uint32_t addr) {//检查给定的地址 addr 是否在 map 指定的地址范围内
  return (addr >= map->low && addr <= map->high);
}

static inline int find_mapid_by_addr(IOMap *maps, int size, uint32_t addr) {
  int i;
  for (i = 0; i < size; i ++) {//查找地址 addr 在 maps 数组中对应的条目索引
    if (map_inside(maps + i, addr)) {
      difftest_skip_ref();
      return i;
    }
  }
  return -1;
}

void add_pio_map(const char *name, ioaddr_t addr,
        void *space, uint32_t len, io_callback_t callback);
void add_mmio_map(const char *name, uint32_t addr,
        void *space, uint32_t len, io_callback_t callback);

uint32_t map_read(uint32_t addr, int len, IOMap *map);//map_read()和map_write()用于将地址addr映射到map所指示的目标空间, 并进行访问
void map_write(uint32_t addr, int len, uint32_t data, IOMap *map);

#endif