
#include <cassert>
#include "map.h"
//#include <memory/paddr.h>

#define NR_MAP 16

static IOMap maps[NR_MAP] = {};
static int nr_map = 0;

static IOMap* fetch_mmio_map(uint32_t addr) {
  int mapid = find_mapid_by_addr(maps, nr_map, addr);
  return (mapid == -1 ? NULL : &maps[mapid]);
}

static void report_mmio_overlap(const char *name1, uint32_t l1, uint32_t r1,
    const char *name2, uint32_t l2, uint32_t r2) {
  printf("MMIO region %s@[" FMT_PADDR ", " FMT_PADDR "] is overlapped "
               "with %s@[" FMT_PADDR ", " FMT_PADDR "]", name1, l1, r1, name2, l2, r2);
}

/* device interface */
void add_mmio_map(const char *name, uint32_t addr, void *space, uint32_t len, io_callback_t callback) {
  assert(nr_map < NR_MAP);
  uint32_t left = addr, right = addr + len - 1;
  if (in_pmem(left) || in_pmem(right)) {
    report_mmio_overlap(name, left, right, "pmem", PMEM_LEFT, PMEM_RIGHT);
  }
  for (int i = 0; i < nr_map; i++) {
    if (left <= maps[i].high && right >= maps[i].low) {
      report_mmio_overlap(name, left, right, maps[i].name, maps[i].low, maps[i].high);
    }
  }

  maps[nr_map] = (IOMap){ .name = name, .low = addr, .high = addr + len - 1,
    .space = space, .callback = callback };
  Log("Add mmio map '%s' at [" FMT_PADDR ", " FMT_PADDR "]",
      maps[nr_map].name, maps[nr_map].low, maps[nr_map].high);

  nr_map ++;
}

/* bus interface */
uint32_t mmio_read(uint32_t addr, int len) {//PIO 读取通常使用 I/O 端口地址进行寻址，这通常需要特殊的 CPU 指令来访问
  return map_read(addr, len, fetch_mmio_map(addr));
}

void mmio_write(uint32_t addr, int len, uint32_t data) {//MMIO 读取使用普通的内存地址进行寻址，可以使用标准的内存访问指令
  map_write(addr, len, data, fetch_mmio_map(addr));
}
