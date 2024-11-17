#include <stdlib.h>
#include <getopt.h>
#include <cstddef>
#include <cstdio>
#include <stdint.h>
#include "common.h"
#include "log.h"

// 包含Verilator提供的verilated.h头文件，这是进行Verilog模拟所必需的
#include <verilated.h>
// 包含由Verilator从Verilog代码生成的C++头文件，通常命名为VysyxSoCFull.h，它包含了模拟所需的类定义
#include "VysyxSoCFull.h"
// INCLUDE MODULE CLASSES
#include "VysyxSoCFull___024root.h"

//static char *img_file = NULL;
extern char *img_file;
extern char *log_file;
extern char *diff_so_file;
extern char *elf_file;
//extern uint8_t buffer[1024];

int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
    {"elf"      , required_argument, NULL, 'e'},
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:p:e:", table, NULL)) != -1) {
    switch (o) {
      case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case  1 : img_file = optarg; return 0;
      case 'e': //printf("zheli\n"); 
                elf_file = optarg; break;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\t-e,--elf=FILE           parse the elf file\n");
        printf("\n");
        exit(0);
    }
  }
  printf("%p\n %s\n", diff_so_file, diff_so_file);
  return 0;
}

//static uint8_t buffer[1024]; // 定义一个缓冲区，用于存储读取的数据
long load_img() {//加载二进制指令
  if (img_file == NULL) {
    printf("No image is given. Use the default build-in image.\n");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  if(fp == NULL)
    {
        printf("failed to open the img_file!\n");
        exit(0);
    }

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  printf("The image is %s, size = %ld\n", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  /*for (int i = 0; i <= 50; i = i + 4){
    printf("Instr %02x %02x %02x %02x\n", buffer[i+3], buffer[i+2], buffer[i+1], buffer[i]);
  }
  uint32_t a = 0x00000008;
  uint32_t rd = buffer[a+3] << 24 | buffer[a+2] << 16 | buffer[a+1] << 8 | buffer[a];
  printf("Instr %08x\n", rd);*/
  if(ret != 1)
    {
        printf("failed to open the img_file!\n");
        exit(0);
    }

  fclose(fp);
  return size;
}

/*extern "C" uint32_t pmem_read(uint32_t raddr) {
  printf("raddr %08x\n", raddr);
  raddr = raddr - 0x80000000;
  uint32_t rdata = (buffer[raddr+3] << 24 | buffer[raddr+2] << 16 | buffer[raddr+1] << 8 | buffer[raddr]);
  printf("rdata %08x\n", rdata);
  return rdata;
}*/

/*extern "C" void pmem_write(uint32_t waddr, uint32_t wdata) {
  printf("waddr %08x\n", waddr);
  printf("wdata %08x\n", wdata);
  buffer[waddr+3] = (wdata & 0xFF000000) >> 24;
  buffer[waddr+2] = (wdata & 0xFF000000) >> 16;
  buffer[waddr+1] = (wdata & 0xFF000000) >> 8;
  buffer[waddr]   = (wdata & 0xFF000000);
}*/

extern VysyxSoCFull *top;

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() {
    int length =  32;
    for(int i = 0  ; i < length ; i ++) {
        printf("reg$%s ---> 0x%08x\n",regs[i], top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__ifu__DOT__rf__DOT___GEN[i]);
    }
}

uint32_t isa_reg_str2val(const char *s, bool *success) {
    int length =  32;
    uint32_t term;
    for(int i = 0  ; i < length ; i ++) {
        if(strcmp(s, regs[i]) == 0) {
            *success = true;
            term = top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__ifu__DOT__rf__DOT___GEN[i];
            //printf("%d\n", term);
            return term;
        }
    }
    assert(0);
    return 0;
}
