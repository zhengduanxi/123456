# Makefile for AbstractMachine Kernels and Libraries

### *Get a more readable version of this Makefile* by `make html` (requires python-markdown)
#这个 Makefile 片段定义了一个名为 "html" 的规则，用于生成 HTML 版本的当前 Makefile
html:
	cat Makefile | sed 's/^\([^#]\)/    \1/g' | markdown_py > Makefile.html
.PHONY: html

#Makefile 中的变量可以从多个地方获取值：\
#Makefile 内部设置： 可能在 Makefile 的其他地方设置了 $(AM_HOME) 变量，但你在当前查看的部分没有找到。\
#外部设置： $(AM_HOME) 可能是在调用 Make 命令时通过命令行参数设置的，例如：\
#make AM_HOME=/path/to/abstractmachine\
#这样会将 $(AM_HOME) 变量设置为 /path/to/abstractmachine。\
#Shell 环境变量： Make 可以访问系统环境变量。如果 $(AM_HOME) 没有在 Makefile 中显式设置，它可能会从操作系统的环境变量中获取值。\

## 1. Basic Setup and Checks基本设置和检查

### Default to create a bare-metal kernel image
#默认情况下，如果没有指定构建目标，将创建一个裸机内核映像
#这是一个条件语句，用于检查是否有指定的构建目标。$(MAKECMDGOALS) 是一个特殊的变量，它包含了用户在命令行中指定的构建目标。
#如果条件为真，即没有指定构建目标，那么这一行会将变量 MAKECMDGOALS 设置为 "image"。
#一般来说，MAKECMDGOALS 用于获取用户在命令行上指定的目标，而 .DEFAULT_GOAL 用于设置默认的构建目标。
#设置它们都是为了确保在没有明确指定目标时，有一个默认的目标可以被构建。
ifeq ($(MAKECMDGOALS),)
  MAKECMDGOALS  = image
  .DEFAULT_GOAL = image
endif

### Override checks when `make clean/clean-all/html`
#假设用户在命令行中运行以下命令之一：
#make clean: 清理项目，删除临时文件等。
#make clean-all: 清理项目，删除所有生成的文件。
#make html: 生成 HTML 版本的 Makefile。
#那么这段代码将会执行相应的操作。
ifeq ($(findstring $(MAKECMDGOALS),clean|clean-all|html),)

### Print build info message
#$(info ...)：$(info) 是一个 Makefile 内置函数，用于输出信息消息到标准输出（通常是终端）。
# Building $(NAME)-$(MAKECMDGOALS) [$(ARCH)]：这是要输出的信息消息的文本内容。它包含了以下变量：
#$(NAME)：这是一个在 Makefile 中定义的变量，通常用于表示构建的项目名称。
#$(MAKECMDGOALS)：这是一个包含用户在命令行上指定的构建目标的变量。
#$(ARCH)：这是另一个在 Makefile 中定义的变量，通常表示目标体系结构或架构。$(ARCH) 的值将是 "riscv32-nemu
$(info # Building $(NAME)-$(MAKECMDGOALS) [$(ARCH)])

### Check: environment variable `$AM_HOME` looks sane
#$(wildcard ...) 是一个 Makefile 函数，用于查找文件系统中的文件。在这里，它用于检查 $(AM_HOME)/am/include/am.h 文件是否存在。
#如果 am.h 文件不存在，条件为真，执行下面的操作。
ifeq ($(wildcard $(AM_HOME)/am/include/am.h),)
  $(error $$AM_HOME must be an AbstractMachine repo)
endif

### Check: environment variable `$ARCH` must be in the supported list
#$(shell ls $(AM_HOME)/scripts/*.mk) 会列出 $(AM_HOME)/scripts 目录下的所有 .mk 文件，并返回文件列表。
#$(notdir ...) 会提取这些文件列表中的文件名，去掉路径部分，只留下文件名。
#$(basename ...) 会去掉文件名中的扩展名，只留下文件的基本名称。
#$(filter $(ARCHS), $(ARCH)) 是一个条件判断，用于检查 $(ARCH) 是否在 $(ARCHS) 列表中。
#$(filter ...) 函数返回列表 $(ARCHS) 中包含的值，如果 $(ARCH) 不在列表中，这个函数会返回一个空字符串。
#ifeq 条件语句用于判断 $(filter ...) 的返回值是否为空。如果为空，表示用户指定的 $(ARCH) 不在支持的架构列表中。
ARCHS = $(basename $(notdir $(shell ls $(AM_HOME)/scripts/*.mk)))
ifeq ($(filter $(ARCHS), $(ARCH)), )
  $(error Expected $$ARCH in {$(ARCHS)}, Got "$(ARCH)")
endif

### Extract instruction set architecture (`ISA`) and platform from `$ARCH`. Example: `ARCH=x86_64-qemu -> ISA=x86_64; PLATFORM=qemu`
ARCH_SPLIT = $(subst -, ,$(ARCH))#用空格替换 ARCH 变量中的短横线（"-"）
ISA        = $(word 1,$(ARCH_SPLIT))#riscv32
PLATFORM   = $(word 2,$(ARCH_SPLIT))#nemu

### Check if there is something to build
#SRCS源代码文件的列表
ifeq ($(flavor SRCS), undefined)
  $(error Nothing to build)
endif

### Checks end here
endif

## 2. General Compilation Targets一般编译目标

### Create the destination directory (`build/$ARCH`)
WORK_DIR  = $(shell pwd)#当前 Makefile 的位置。
DST_DIR   = $(WORK_DIR)/build/$(ARCH)
$(shell mkdir -p $(DST_DIR))#-p 选项确保如果目录已经存在，就不会触发错误

### Compilation targets (a binary image or archive)
#编译目标(二进制映像或存档)包括二进制可执行文件（IMAGE）和静态库文件（ARCHIVE）
IMAGE_REL = build/$(NAME)-$(ARCH)
IMAGE     = $(abspath $(IMAGE_REL))#$(abspath ...) 函数，将 IMAGE_REL 变量的相对路径转化为绝对路径。它表示最终的可执行文件的完整路径。
ARCHIVE   = $(WORK_DIR)/build/$(NAME)-$(ARCH).a

### Collect the files to be linked: object files (`.o`) and libraries (`.a`)
#收集要链接的文件，包括目标文件（.o）和静态库文件（.a）
OBJS      = $(addprefix $(DST_DIR)/, $(addsuffix .o, $(basename $(SRCS))))#addprefix 函数，将 DST_DIR 目录下的目标文件的完整路径列出。它是所有目标文件（.o）的列表。
LIBS     := $(sort $(LIBS) am klib) # lazy evaluation ("=") causes infinite recursions #这个变量是用于指定需要链接的库文件的列表，包括 am 和 klib，
LINKAGE   = $(OBJS) \
  $(addsuffix -$(ARCH).a, $(join \
    $(addsuffix /build/, $(addprefix $(AM_HOME)/, $(LIBS))), \
    $(LIBS) ))#LINKAGE：这个变量定义了最终链接时的文件列表，包括目标文件 $(OBJS) 和静态库文件

## 3. General Compilation Flags

### (Cross) compilers, e.g., mips-linux-gnu-g++
AS        = $(CROSS_COMPILE)gcc#汇编器
CC        = $(CROSS_COMPILE)gcc
CXX       = $(CROSS_COMPILE)g++
LD        = $(CROSS_COMPILE)ld
AR        = $(CROSS_COMPILE)ar
OBJDUMP   = $(CROSS_COMPILE)objdump#用于反汇编的工具
OBJCOPY   = $(CROSS_COMPILE)objcopy#二进制文件转换的工具
READELF   = $(CROSS_COMPILE)readelf#读取 ELF 文件信息的工具

### Compilation flags
#addsuffix 和 addprefix 是 GNU Make 中的两个常用函数，用于在字符串列表的每个元素前面或后面添加相同的前缀或后缀。
INC_PATH += $(WORK_DIR)/include $(addsuffix /include/, $(addprefix $(AM_HOME)/, $(LIBS)))
INCFLAGS += $(addprefix -I, $(INC_PATH))#这一行使用 -I 标志将 INC_PATH 中的路径前缀添加到 INCFLAGS 中。这意味着编译器在查找头文件时将搜索这些路径。 -I 标志告诉编译器在这些路径中查找头文件，以便在编译源代码时正确引用依赖的库和头文件。

#CFLAGS：C 编译器选项，包括了 -O2（启用优化级别 2），-MMD（生成依赖关系文件），-Wall（启用所有警告），-Werror（将警告视为错误），$(INCFLAGS)（包含额外的头文件路径），和一系列预处理宏定义，例如 __ISA__、__ARCH__ 和 __PLATFORM__。这些宏定义将用于源代码中的条件编译和控制编译过程。

#CXXFLAGS：C++ 编译器选项，继承自 CFLAGS，但还包括了 -ffreestanding（告诉编译器生成无操作系统环境的代码），-fno-rtti（禁用运行时类型信息），和 -fno-exceptions（禁用 C++ 异常处理）等选项。

#ASFLAGS：汇编器选项，包括了 -MMD（生成依赖关系文件）和 $(INCFLAGS)（包含额外的头文件路径）。

#LDFLAGS：链接器选项，包括了 -z noexecstack（禁止在堆栈上执行代码）。

#传入不同的ARCH参数会调用不同的编译器

#cc 编译时 使用 -fno-builtin 告诉编译器不要使用内置函数优化,选项就可以使用自己编写的库函数同名函数

#-D__ISA__=\"$(ISA)\"：定义宏 __ISA__，并将其值设置为变量 $(ISA) 的值，这个值是字符串形式的。

#-D__ISA_$(shell echo $(ISA) | tr a-z A-Z)__：定义宏 __ISA_ 后跟 $(ISA) 值的转换形式，这里使用 tr 命令将小写字母转换为大写字母。

#-D__PLATFORM__=$(PLATFORM) 和 -D__PLATFORM_$(shell echo $(PLATFORM) | tr a-z A-Z | tr - _)：定义宏 __PLATFORM__ 和 __PLATFORM_，与 __ARCH__ 和 __ARCH_ 类似，但用于 $(PLATFORM) 变量。

#-DARCH_H=\"$(ARCH_H)\"：定义宏 ARCH_H，其值为 $(ARCH_H) 变量的值，这里使用了字符串形式。

ARCH_H := arch/$(ARCH).h
CFLAGS   += -O2 -MMD -Wall -Werror $(INCFLAGS) \
            -D__ISA__=\"$(ISA)\" -D__ISA_$(shell echo $(ISA) | tr a-z A-Z)__ \
            -D__ARCH__=$(ARCH) -D__ARCH_$(shell echo $(ARCH) | tr a-z A-Z | tr - _) \
            -D__PLATFORM__=$(PLATFORM) -D__PLATFORM_$(shell echo $(PLATFORM) | tr a-z A-Z | tr - _) \
            -DARCH_H=\"$(ARCH_H)\" \
            -fno-asynchronous-unwind-tables -fno-builtin -fno-stack-protector \
            -Wno-main -U_FORTIFY_SOURCE
CXXFLAGS +=  $(CFLAGS) -ffreestanding -fno-rtti -fno-exceptions
ASFLAGS  += -MMD $(INCFLAGS)
LDFLAGS  += -z noexecstack

## 4. Arch-Specific Configurations选择对应的架构配置

### Paste in arch-specific configurations (e.g., from `scripts/x86_64-qemu.mk`)
-include $(AM_HOME)/scripts/$(ARCH).mk#选择nemu对应的架构mk文件

### Fall back to native gcc/binutils if there is no cross compiler
ifeq ($(wildcard $(shell which $(CC))),)
  $(info #  $(CC) not found; fall back to default gcc and binutils)
  CROSS_COMPILE :=
endif

## 5. Compilation Rules

### Rule (compile): a single `.c` -> `.o` (gcc)
$(DST_DIR)/%.o: %.c
	@mkdir -p $(dir $@) && echo + CC $<
	@$(CC) -std=gnu11 $(CFLAGS) -c -o $@ $(realpath $<)

### Rule (compile): a single `.cc` -> `.o` (g++)
$(DST_DIR)/%.o: %.cc
	@mkdir -p $(dir $@) && echo + CXX $<
	@$(CXX) -std=c++17 $(CXXFLAGS) -c -o $@ $(realpath $<)

### Rule (compile): a single `.cpp` -> `.o` (g++)
$(DST_DIR)/%.o: %.cpp
	@mkdir -p $(dir $@) && echo + CXX $<
	@$(CXX) -std=c++17 $(CXXFLAGS) -c -o $@ $(realpath $<)

### Rule (compile): a single `.S` -> `.o` (gcc, which preprocesses and calls as)
$(DST_DIR)/%.o: %.S
	@mkdir -p $(dir $@) && echo + AS $<
	@$(AS) $(ASFLAGS) -c -o $@ $(realpath $<)

### Rule (recursive make): build a dependent library (am, klib, ...)
$(LIBS): %:
	@$(MAKE) -s -C $(AM_HOME)/$* archive

### Rule (link): objects (`*.o`) and libraries (`*.a`) -> `IMAGE.elf`, the final ELF binary to be packed into image (ld)
$(IMAGE).elf: $(OBJS) am $(LIBS)
	@echo + LD "->" $(IMAGE_REL).elf
	@$(LD) $(LDFLAGS) -o $(IMAGE).elf --start-group $(LINKAGE) --end-group

### Rule (archive): objects (`*.o`) -> `ARCHIVE.a` (ar)
$(ARCHIVE): $(OBJS)
	@echo + AR "->" $(shell realpath $@ --relative-to .)
	@$(AR) rcs $(ARCHIVE) $(OBJS)

### Rule (`#include` dependencies): paste in `.d` files generated by gcc on `-MMD`
-include $(addprefix $(DST_DIR)/, $(addsuffix .d, $(basename $(SRCS))))

## 6. Miscellaneous

### Build order control
image: image-dep
archive: $(ARCHIVE)
image-dep: $(OBJS) am $(LIBS)
	@echo \# Creating image [$(ARCH)]
.PHONY: image image-dep archive run $(LIBS)

### Clean a single project (remove `build/`)
clean:
	rm -rf Makefile.html $(WORK_DIR)/build/
.PHONY: clean

### Clean all sub-projects within depth 2 (and ignore errors)
CLEAN_ALL = $(dir $(shell find . -mindepth 2 -name Makefile))
clean-all: $(CLEAN_ALL) clean
$(CLEAN_ALL):
	-@$(MAKE) -s -C $@ clean
.PHONY: clean-all $(CLEAN_ALL)
