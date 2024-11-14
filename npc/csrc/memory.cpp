#include <cstdlib>
#include <cassert>
#include <cstring>
#include "common.h"
#include "log.h"
/*extern "C" int pmem_read(uint32 raddr) {
  // 总是读取地址为`raddr & ~0x3u`的4字节返回
}
extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
}*/
//static uint8_t *pmem = NULL;
#if   defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif

uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }//虚拟地址转换为宿主机地址
uint32_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }//宿主机地址转换为虚拟地址

bool in_pmem(uint32_t addr) {
  return addr - CONFIG_MBASE < CONFIG_MSIZE;
}

uint32_t host_read(void *addr, int len) {
  switch (len) {
    case 1: return *(uint8_t  *)addr;
    case 2: return *(uint16_t *)addr;
    case 4: return *(uint32_t *)addr;
    default: return 0;
  }
}

void host_write(void *addr, int len, uint32_t data) {
  switch (len) {
    case 1: *(uint8_t  *)addr = data; return;
    case 2: *(uint16_t *)addr = data; return;
    case 4: *(uint32_t *)addr = data; return;
  }
}

extern "C" uint32_t pmem_read(uint32_t addr, int len) {
  uint32_t ret = host_read(guest_to_host(addr), len);
  #ifdef CONFIG_MTRACE
  display_memory_read(addr, len, ret);
  #endif
  return ret;
}//从物理内存中读取数据

extern "C" void pmem_write(uint32_t addr, int len, uint32_t data) {
  #ifdef CONFIG_MTRACE
  display_memory_write(addr, len, data);
  #endif
  host_write(guest_to_host(addr), len, data);
}//向物理内存中写入数据

/* extern "C" uint32_t pmem_read(uint32_t raddr) {
  // 总是读取地址为`raddr & ~0x3u`的4字节返回
  uint32_t ret = *(uint32_t *)(guest_to_host(raddr & ~0x3u));
  return ret;
}

extern "C" void pmem_write(uint32_t waddr, uint32_t wdata, char wmask) {
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
  uint32_t term = *(uint32_t *)(guest_to_host(waddr & ~0x3u));
  //wdata = wdata & wmask | ret & ~wmask;
  for (int i = 0; i < 4; i++) {
        if (wmask & (1 << i)) {
            // 计算字节偏移量
            int byte_offset = i * 8;
            // 清除目标地址对应字节的位
            term &= ~(0xFF << byte_offset);
            // 设置新的字节
            term |= wdata & (0xFF << byte_offset);
            // ((wdata >> byte_offset) & 0xFF) << byte_offset;
        }
    }

  *(uint32_t *)(guest_to_host(waddr & ~0x3u)) = term;
} */

extern CPU_state cpu;
extern "C" void get_cpu_pc(uint32_t PCNext) {
  cpu.pc = PCNext;
}

static void out_of_bound(uint32_t addr) {
  printf("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, cpu.pc);
}

extern "C" uint32_t paddr_read(uint32_t addr, int len) {
  if (likely(in_pmem(addr))) return pmem_read(addr, len);
  #ifdef CONFIG_DEVICE
  return mmio_read(addr, len);
  #endif
  out_of_bound(addr);
  return 0;
}

extern "C" void paddr_write(uint32_t addr, int len, uint32_t data) {
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  #ifdef CONFIG_DEVICE
  mmio_write(addr, len, data); return;
  #endif
  out_of_bound(addr);
}

/*void init_mem() {
  pmem = (uint8_t*)malloc(CONFIG_MSIZE);
  assert(pmem);
}*/
void init_mem() {
#if   defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem);
#endif
  #ifdef CONFIG_MEM_RANDOM
  memset(pmem, rand(), CONFIG_MSIZE);
  #endif
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
}