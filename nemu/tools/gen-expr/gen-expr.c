/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static int index_buf = 0;
static char buf[65536] = {};
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";

static int choose(int n) {
    int flag = rand() % n ; // 0 1 2
	//printf("index = %d, flag = %d. \n",index_buf, flag);
    return flag;
}

static void gen_num() {
    int num = choose(9) + 1; // 生成1到9之间的随机数
    buf[index_buf++] = '0' + num;//将数字转化为字符串
}

static void gen(char c) {
    buf[index_buf++] = c;
}

static void gen_rand_op() {
    switch (choose(4)) {
        case 0:
            gen('+');
            break;
        case 1:
            gen('-');
            break;
        case 2:
            gen('*');
            break;
        case 3:
            gen('/');
            break;
    }
}

static void gen_rand_expr() {
    //    buf[0] = '\0';
   int count = 0;	
   if(index_buf > 65536)
   {
       	memset(buf, 0, sizeof(buf));
       	index_buf = 0;
       	//printf("overSize\n");
       	return ;
   }
     switch (choose(3)) {
	case 0:
	    switch (choose(2)){
	           case 0:
	                gen_num();
	                break;
	           default:
	                gen_num();
	                gen_num();
	                break;
	           }
	    break;
	case 1:
	    gen('(');
	    gen_rand_expr();
	    gen(')');
	    break;
	default:
	    gen_rand_expr();
	    gen_rand_op();
	    gen_rand_expr();
	    break;
    }
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {

    memset(buf, 0, sizeof(buf));
    index_buf = 0;

    gen_rand_expr();

    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc -Werror /tmp/.code.c -o /tmp/.expr");
    if (ret != 0) {
    i--;
    continue;
    }

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    pclose(fp);

    printf("%u %s\n", result, buf);
  }
  return 0;
}

