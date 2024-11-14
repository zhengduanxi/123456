//#include <utils.h>
#include <cassert>
#include "map.h"

/* http://en.wikibooks.org/wiki/Serial_Programming/8250_UART_Programming */
// NOTE: this is compatible to 16550

#define CH_OFFSET 0

static uint8_t *serial_base = NULL;


static void serial_putc(char ch) {//根据 CONFIG_TARGET_AM 调用 putch 或 putc
  #ifdef CONFIG_TARGET_AM
  putch(ch);
  #else
  putc(ch, stderr);
  #endif
}

static void serial_io_handler(uint32_t offset, int len, bool is_write) {//回调函数则调用了 serial_putc
  assert(len == 1);
  switch (offset) {
    /* We bind the serial port with the host stderr in NEMU. */
    case CH_OFFSET:
      if (is_write) serial_putc(serial_base[0]);
      else //printf("do not support read");
      break;
    default: break; //printf("do not support offset = %d", offset);
  }
}

void init_serial() {
  serial_base = new_space(8);
#ifdef CONFIG_HAS_PORT_IO//根据 I/O 编址方式注册端口或者分配空间
  add_pio_map ("serial", CONFIG_SERIAL_PORT, serial_base, 8, serial_io_handler);
#else
  add_mmio_map("serial", CONFIG_SERIAL_MMIO, serial_base, 8, serial_io_handler);
#endif

}
