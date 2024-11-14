#include <am.h>
#include <nemu.h>

void __am_timer_init() {
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  uint32_t hi = inl(RTC_ADDR + 4);//在这里需要先读取高位，因为在rtc_io_handler中具有offset==4的判断
  uint32_t lo = inl(RTC_ADDR + 0);//在读取高位时，offfset为4，才会触发get_time()获取最新的时间
  uint64_t time = ((uint64_t)hi << 32) | lo;//如果先读取低位的话，那么读取的是上次的时间，导致错误
  uptime->us = time;
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}
