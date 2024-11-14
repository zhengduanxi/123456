#include <elf.h>
#include <cstdlib>
#include <cstring>
#include "common.h"
#include "log.h"

#define INST_NUM 16

// iringbuf
typedef struct
{
    uint32_t pc;
    uint32_t inst;
}InstBuf;

InstBuf iringbuf[INST_NUM];

static int cur_inst = 0;

extern "C" void trace_inst(uint32_t pc, uint32_t inst)
{
    iringbuf[cur_inst].pc = pc;
    //printf("%08x\t", iringbuf[cur_inst].pc);
    iringbuf[cur_inst].inst = inst;
    //printf("%08x\n", iringbuf[cur_inst].inst);
    cur_inst = (cur_inst + 1) % INST_NUM; 
}

void display_inst()
{
    /*** 注意出错的是前一条指令，当前指令可能由于出错已经无法正常译码 ***/
    int end = cur_inst;
    char buf[128];
    char *p;
    int i = cur_inst;

    if(iringbuf[i+1].pc == 0) i = 0;//下面是空的，没满16条指令,i从头开始

    do//i从错误指令的下一条开始
    {
        p = buf;
        //if(i == end) p += sprintf(buf, "-->");
        //p += snprintf(p, sizeof(buf), FMT_WORD ":", iringbuf[i].pc);
        p += sprintf(buf, "%s" FMT_WORD ":  %08x\t", (i + 1) % INST_NUM == end ? "-->" : "   ", iringbuf[i].pc, iringbuf[i].inst);

        /*int ilen = iringbuf[i+1].pc - iringbuf[i].pc;
        int n;
        uint8_t *inst = (uint8_t *)&iringbuf[i].inst;
        for (n = ilen - 1; n >= 0; n --) {
          p += snprintf(p, 4, " %02x", inst[n]);
          printf(" %02x\n", inst[n]);
        }
        int ilen_max = 4;
        int space_len = ilen_max - ilen;
        if (space_len < 0) space_len = 0;
        space_len = space_len * 3 + 1;
        memset(p, ' ', space_len);
        p += space_len;*/
        void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
        disassemble(p, buf + sizeof(buf) - p, iringbuf[i].pc, (uint8_t *)&iringbuf[i].inst, 4);

        puts(buf);
        i = (i + 1) % INST_NUM;
    } while (i != end);
     
}

//mtrace
extern "C" void display_memory_read(uint32_t addr, int len, uint32_t data)
{
    printf(ANSI_FMT("read memory: ", ANSI_FG_BLUE) FMT_PADDR ", the len is %d, the read data is " FMT_WORD "\n", addr, len, data);
}

extern "C" void display_memory_write(uint32_t addr, int len, uint32_t data)
{
    printf(ANSI_FMT("write memory: ", ANSI_FG_YELLOW) FMT_PADDR ", the len is %d, the written data is " FMT_WORD "\n", addr, len, data);
}

//ftrace
typedef struct {
    char name[64];
    uint32_t addr;      //the function head address
    Elf32_Xword size;
} Symbol;//符号的名称、地址和大小信息

Symbol *symbol = NULL;  //dynamic allocate memory  or direct allocate memory (Symbol symbol[NUM])

int func_num = 0;

void parse_elf(const char *elf_file)//const char *elf_file 参数表示要解析的 ELF 文件路径
{
    
    if(elf_file == NULL) {printf("zheli\n"); return;}
    
    FILE *fp;
    fp = fopen(elf_file, "rb");
    
    if(fp == NULL)
    {
        printf("failed to open the elf file!\n");
        exit(0);
    }
	
    Elf32_Ehdr edhr;//Elf32_Ehdr 结构体，声明一个 Elf32_Ehdr 类型的变量 ehdr 用于存储ELF 文件头信息
	//读取elf头
    if(fread(&edhr, sizeof(Elf32_Ehdr), 1, fp) <= 0)
    {
        printf("fail to read the elf_head!\n");
        exit(0);
    }

    if(edhr.e_ident[0] != 0x7f || edhr.e_ident[1] != 'E' || //E->45 L->4c F->46
       edhr.e_ident[2] != 'L' ||edhr.e_ident[3] != 'F')//e_ident: 一个数组，包含了ELF标识信息，用于识别文件是否为ELF格式以及其字节序（大端或小端）。
    {
        printf("The opened file isn't a elf file!\n");
        exit(0);
    }
    
    fseek(fp, edhr.e_shoff, SEEK_SET);//定位到文件的节头表（section header table）的起始位置，SEEK_SET：从文件开头开始，从文件开头向后移动edhr.e_shoff个字节
    //e_shoff: 节头表的文件偏移量，如果文件中没有节头表，则此值为0（例如，核心转储）

    Elf32_Shdr shdr;//Elf32_Shdr 结构体，声明一个 Elf32_Shdr 类型的变量 shdr 用于存储节头信息
    char *string_table = NULL;
    //寻找字符串表
    for(int i = 0; i < edhr.e_shnum; i++)//e_shnum: 节头表中的条目数量，某些文件可能不包含节头表，此时这个值是 SHN_UNDEF
    {
        if(fread(&shdr, sizeof(Elf32_Shdr), 1, fp) <= 0)
        {
            printf("fail to read the shdr\n");
            exit(0);
        }
        
        if(shdr.sh_type == SHT_STRTAB)//循环遍历所有的节头，寻找字符串表（SHT_STRTAB 类型）
        //sh_type: 节的类型，例如 SHT_PROGBITS 表示程序定义的数据，SHT_SYMTAB 表示符号表等
        {
            //获取字符串表
            string_table = (char*)malloc(shdr.sh_size);//如果找到字符串表，分配内存并读取字符串表的内容
            fseek(fp, shdr.sh_offset, SEEK_SET);//sh_offset: 节在文件中的偏移量;shdr.sh_offset是字符串表在文件中的起始偏移量
            if(fread(string_table, shdr.sh_size, 1, fp) <= 0)//sh_size: 节的大小
            {
                printf("fail to read the strtab\n");
                exit(0);
            }
        }
    }
    
    //寻找符号表
    fseek(fp, edhr.e_shoff, SEEK_SET);//再次定位到节头表的起始位置
    
    for(int i = 0; i < edhr.e_shnum; i++)
    {
        if(fread(&shdr, sizeof(Elf32_Shdr), 1, fp) <= 0)
        {
            printf("fail to read the shdr\n");
            exit(0);
        }

        if(shdr.sh_type == SHT_SYMTAB)//循环遍历所有的节头，寻找符号表（SHT_SYMTAB 类型）
        {
            fseek(fp, shdr.sh_offset, SEEK_SET);//shdr.sh_offset 是符号表在文件中的偏移量

            Elf32_Sym sym;

            size_t sym_count = shdr.sh_size / shdr.sh_entsize;//计算符号表中的符号数量;sh_size:This  member  holds  the  section's size in bytes符号表的总大小;sh_entsize: 节中每个条目table的大小，如果节包含固定大小的条目
            symbol = (Symbol*)malloc(sizeof(Symbol) * sym_count);//如果找到符号表，分配内存并读取符号表的内容

            for(size_t j = 0; j < sym_count; j++)
            {
                if(fread(&sym, sizeof(Elf32_Sym), 1, fp) <= 0)
                {
                    printf("fail to read the symtab\n");
                    exit(0);
                }

                if(ELF32_ST_TYPE(sym.st_info) == STT_FUNC)//循环遍历符号表中的每个符号，检查符号类型是否为函数（STT_FUNC）,Elf32_Sym 结构体中的 st_info 字段是一个重要的位字段，它由两个部分组成：STT_*,STB_*
                {
                    const char *name = string_table + sym.st_name;//sym.st_name 的值是字符串表中对应符号名称的字节偏移量。通过将这个偏移量加到 string_table 指针上，可以得到指向该符号名称字符串的指针
                    strncpy(symbol[func_num].name, name, sizeof(symbol[func_num].name) - 1);//如果是函数符号，则从字符串表中获取符号名称，并将其复制到 symbol 数组中
                    symbol[func_num].addr = sym.st_value;//将符号的地址和大小信息也存储到 symbol 数组中
                    symbol[func_num].size = sym.st_size;
                    //printf("call  [%s]\n", symbol[i].name);
                    //printf("ret  [%s]\n", symbol[i].name);
                    func_num++;//更新 func_num 变量，表示已解析的函数数量
                }
            }
        }
    }
    fclose(fp);
    free(string_table);
    int n = 0;
    for(; n < func_num; n++) {
        printf("call  [%s]\n", symbol[n].name);
        printf("ret  [%s]\n", symbol[n].name);
    }
}

int rec_depth = 1;
extern "C" void display_call_func(uint32_t pc, uint32_t func_addr)
{
    /*for(int i = 0; i < func_num; i++)
    {
        printf("%s\t0x%08x\t%lu\n", symbol[i].name, symbol[i].addr, symbol[i].size);
    }
    exit(0);*/
    int i = 0;
    for(; i < func_num; i++)
    {
        if(func_addr >= symbol[i].addr && func_addr < (symbol[i].addr + symbol[i].size))//成立就跳出循环，打印
        {
            break;
        }
    }
    printf("0x%08x:", pc);

    for(int k = 0; k < rec_depth; k++) printf("  ");

    rec_depth++;

    printf("call  [%s@0x%08x]\n", symbol[i].name, func_addr);
}

extern "C" void display_ret_func(uint32_t pc)
{
    int i = 0;
    for(; i < func_num; i++)
    {
        if(pc >= symbol[i].addr && pc < (symbol[i].addr + symbol[i].size))
        {
            break;
        }
    }
    printf("0x%08x:", pc);

    rec_depth--;

    for(int k = 0; k < rec_depth; k++) printf("  ");

    printf("ret  [%s]\n", symbol[i].name);
}

