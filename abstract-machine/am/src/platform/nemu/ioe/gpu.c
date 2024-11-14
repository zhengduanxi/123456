#include <am.h>
#include <nemu.h>
//#include <stdio.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

void __am_gpu_init() {
  uint32_t space = inl(VGACTL_ADDR);
  int i;
  int w = space >> 16;  // TODO: get the correct width
  int h = space & 0x0000ffff;  // TODO: get the correct height
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;//填充一个帧缓冲区（frame buffer）
  for (i = 0; i < w * h; i ++) fb[i] = i;
  outl(SYNC_ADDR, 1);//并将内容同步到显示设备上
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {//屏幕大小寄存器->nemu
  uint32_t space = inl(VGACTL_ADDR);
  int w = space >> 16;
  int h = space & 0x0000ffff;
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = w, .height = h,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {//nemu->同步寄存器
  int x = ctl->x, y = ctl->y, w = ctl->w, h = ctl->h;
  if (!ctl->sync && (w == 0 || h == 0)) return;
  uint32_t *pixels = ctl->pixels;
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  uint32_t screen_w = inl(VGACTL_ADDR) >> 16;
  for (int i = y; i < y+h; i++) {//遍历每一行
    for (int j = x; j < x+w; j++) {//每一行的每一个元素
      fb[screen_w*i+j] = pixels[w*(i-y)+(j-x)];
      //printf("fb[%d] = pixels[%d]\n;", screen_w*i+j, w*(i-y)+(j-x));
      //printf("screen_w %d w=%d\n;", screen_w, w);
    }
  }
  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
