#include "map.h"
#include "../common.h"
#include "../log.h"
//#include <utils.h>
//#include <device/alarm.h>
//#ifndef CONFIG_TARGET_AM
//#include <SDL2/SDL.h>
//#endif

void init_map();
void init_serial();
void init_timer();
/*void init_vga();
void init_i8042();
void init_audio();
void init_disk();
void init_sdcard();
void init_alarm();

void send_key(uint8_t, bool);
void vga_update_screen();*/

#define TIMER_HZ 60
void device_update() {
  static uint64_t last = 0;
  uint64_t now = get_time();//get_time->io_read->ioe_read->__am_timer_uptime
  if (now - last < 1000000 / TIMER_HZ) {//以60帧的速率更新设备状态
    return;
  }
  last = now;

  /*IFDEF(CONFIG_HAS_VGA, vga_update_screen());//->update_screen->io_write->ioe_write->__am_gpu_fbdraw

#ifndef CONFIG_TARGET_AM
  SDL_Event event;
  while (SDL_PollEvent(&event)) {
    switch (event.type) {
      case SDL_QUIT:
        nemu_state.state = NEMU_QUIT;
        break;
#ifdef CONFIG_HAS_KEYBOARD
      // If a key was pressed
      case SDL_KEYDOWN:
      case SDL_KEYUP: {
        uint8_t k = event.key.keysym.scancode;
        bool is_keydown = (event.key.type == SDL_KEYDOWN);
        send_key(k, is_keydown);
        break;
      }
#endif
      default: break;
    }
  }
#endif*/
}

/*void sdl_clear_event_queue() {
#ifndef CONFIG_TARGET_AM
  SDL_Event event;
  while (SDL_PollEvent(&event));
#endif
}*/

void init_device() {
  #ifdef CONFIG_TARGET_AM
  ioe_init();
  #endif

  init_map();//调用init_map()进行初始化

  #ifdef CONFIG_HAS_SERIAL
  init_serial();
  #endif

  #ifdef CONFIG_HAS_TIMER
  init_timer();
  #endif
  /*IFDEF(CONFIG_HAS_VGA, init_vga());
  IFDEF(CONFIG_HAS_KEYBOARD, init_i8042());
  IFDEF(CONFIG_HAS_AUDIO, init_audio());
  IFDEF(CONFIG_HAS_DISK, init_disk());
  IFDEF(CONFIG_HAS_SDCARD, init_sdcard());

  IFNDEF(CONFIG_TARGET_AM, init_alarm());*/
}
