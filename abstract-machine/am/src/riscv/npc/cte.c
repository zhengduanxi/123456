#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    Event ev = {0};
    /*switch (c->mcause) {
      case 0:
        ev.event=EVENT_YIELD;break;
      default: ev.event = EVENT_ERROR; break;
    }*/

    if (c->GPR1 == -1) {//void yield() {asm volatile("li a7, -1; ecall");}
      ev.event = EVENT_YIELD;
    } else if (c->GPR1 >= 0 && c->GPR1 <= 19) {
      ev.event = EVENT_SYSCALL;
    } else {
      ev.event = EVENT_ERROR;
    }

    /*switch (c->mcause) {
      case 0: ev.event = EVENT_SYSCALL; break;
      case -1: ev.event = EVENT_YIELD; break;//void yield() {asm volatile("li a7, 11; ecall");}
      default: ev.event = EVENT_ERROR; break;
    }*/

    c = user_handler(ev, c);//simple_trap(ev, c);

    switch (ev.event) {
      case EVENT_PAGEFAULT:
      case EVENT_ERROR:
        break;
      default: c->mepc += 4;
    }

    assert(c != NULL);
  }

  return c;//返回可能已被用户处理函数更新的上下文对象
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));

  // register event handler
  user_handler = handler;

  return true;
}

Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
  Context *cp = kstack.end - sizeof(Context);
  cp->mepc = (uintptr_t)entry - 4;//mret时会返回到f函数中
  cp->gpr[10] = (uintptr_t)arg;
  cp->mstatus = 0x1800;
  return cp;
}

void yield() {
#ifdef __riscv_e
  asm volatile("li a5, -1; ecall");
#else
  asm volatile("li a7, -1; ecall");
#endif
}

bool ienabled() {
  return false;
}

void iset(bool enable) {
}
