#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

//声明了一个函数指针 user_handler，它可以指向一个用户定义的函数，该函数接受两个参数：一个 Event 类型和一个指向 Context 类型的指针
static Context* (*user_handler)(Event, Context*) = NULL;//并返回一个指向 Context 类型的指针,Context *simple_trap(Event ev, Context *ctx)

Context* __am_irq_handle(Context *c) {//函数调用前需按照Calling Convention传递参数,指令mv a0, sp就是为了传递参数，传递的参数是sp
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
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));//设置mtvec寄存器的值即异常入口地址
//直接将异常入口地址设置到mtvec寄存器中即可,将__am_asm_trap函数的地址写入mtvec寄存器，从而设置异常入口
  // register event handler
  user_handler = handler;//simple_trap(ev, c);

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
#else//将 -1 这个值加载到 a7 寄存器中，然后执行 ecall 指令
  asm volatile("li a7, -1; ecall");
#endif
}

bool ienabled() {
  return false;
}

void iset(bool enable) {
}
