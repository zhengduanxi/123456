#include <readline/readline.h>
#include <readline/history.h>
#include <cstddef>
#include <cstdlib>
#include <cstring>
#include "log.h"
#include "common.h"

// 包含Verilator提供的verilated.h头文件，这是进行Verilog模拟所必需的
#include <verilated.h>
#include <verilated_vcd_c.h>
// 包含由Verilator从Verilog代码生成的C++头文件，通常命名为Vtop.h，它包含了模拟所需的类定义
#include "Vtop.h"
// INCLUDE MODULE CLASSES
#include "Vtop___024root.h"

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}

static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return -1;
}

extern Vtop *top;
extern VerilatedVcdC *m_trace;
static int cmd_q(char *args) {
  //npc_state.state = NPC_QUIT;
  top->final();
  m_trace->close();
  exit(0);
}

//TODO:
static int cmd_si(char *args) {
  char *arg = strtok(args, " ");
  if (arg == NULL){
    cpu_exec(1);
  }
  else{
    int n = atoi(arg);
    cpu_exec(n);
  }
  return 0;
}

static int cmd_info(char *args) {
  char *arg = strtok(args, " ");
  if (arg == NULL);
  else if (strcmp(arg, "r") == 0){
    isa_reg_display();
  }
  else if (strcmp(arg, "w") == 0){
    sdb_watchpoint_display();
  }
  return 0;
}

static int cmd_x(char *args){
  char *arg = strtok(args, " ");
  char *EXPR = strtok(NULL," ");
  if (arg == NULL);
  else{
    if(EXPR == NULL);
    else{
      int len = 0;
      uint32_t addr = 0;
      sscanf(arg, "%d", &len);
      sscanf(EXPR,"%x", &addr);
      for(int i = 0 ; i < len ; i ++)
      { uint32_t data = pmem_read(addr, 4);
        printf("0x%08x:\t", addr);
        printf("0x%08x\n", data);
        addr = addr + 4;
      }
    }
  }
  return 0;
}

static int cmd_p(char* args){
  if(args == NULL);
  else{
    bool flag = false;
    expr(args, &flag);
  }
  return 0;
}

static int cmd_w(char* args){
  create_watchpoint(args);
  return 0;
}

static int cmd_d (char *args){
  if(args == NULL);
  else{
    delete_watchpoint(atoi(args));
  }
  return 0;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },

  /* TODO: Add more commands */
  { "si", "Single Step", cmd_si },
  { "info", "Print Program", cmd_info },
  { "x", "Scanning Memory", cmd_x },
  { "p", "Expr", cmd_p },
  { "w", "Create_watchpoint", cmd_w },
  { "d", "Delete_watchpoint", cmd_d },

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_mainloop() {
  /*if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }*/

  init_sdb();

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

/*#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif*/

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}