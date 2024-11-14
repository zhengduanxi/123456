#include <cassert>
#include <sys/time.h>
#include "map.h"
//#include <device/alarm.h>
//#include <utils.h>

static uint64_t boot_time = 0;

static uint64_t get_time_internal() {
#if defined(CONFIG_TARGET_AM)
  uint64_t us = io_read(AM_TIMER_UPTIME).us;
#elif defined(CONFIG_TIMER_GETTIMEOFDAY)
  struct timeval now;
  gettimeofday(&now, NULL);
  uint64_t us = now.tv_sec * 1000000 + now.tv_usec;
#else
  struct timespec now;
  clock_gettime(CLOCK_MONOTONIC_COARSE, &now);
  uint64_t us = now.tv_sec * 1000000 + now.tv_nsec / 1000;
#endif
  return us;
}

uint64_t get_time() {
  if (boot_time == 0) boot_time = get_time_internal();
  uint64_t now = get_time_internal();
  return now - boot_time;
}

static uint32_t *rtc_port_base = NULL;

static void rtc_io_handler(uint32_t offset, int len, bool is_write) {
  assert(offset == 0 || offset == 4);
  if (!is_write && offset == 4) {
    uint64_t us = get_time();//us通过get_time()获取，get_time()又调用库函数来获取时间
    rtc_port_base[0] = (uint32_t)us;
    rtc_port_base[1] = us >> 32;//然后把获取的时间返回rtc_io_handler()，并复制给rtc寄存器，也就是MMIO空间
  }
}

extern NPCState npc_state;
#ifndef CONFIG_TARGET_AM
static void timer_intr() {
  if (npc_state.state == NPC_RUNNING) {
    extern void dev_raise_intr();
    dev_raise_intr();
  }
}
#endif

void init_timer() {
  rtc_port_base = (uint32_t *)new_space(8);
#ifdef CONFIG_HAS_PORT_IO
  add_pio_map ("rtc", CONFIG_RTC_PORT, rtc_port_base, 8, rtc_io_handler);
#else
  add_mmio_map("rtc", CONFIG_RTC_MMIO, rtc_port_base, 8, rtc_io_handler);
#endif
  #ifdef CONFIG_TARGET_AM
  add_alarm_handle(timer_intr);
  #endif
}
