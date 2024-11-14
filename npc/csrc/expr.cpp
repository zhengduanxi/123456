#include <regex.h>
#include <cstring>
#include <cstdio>
#include <cstdint>
#include <cassert>
#include <stdlib.h>
#include "log.h"
#include "common.h"

enum {
  TK_NOTYPE = 256, TK_EQ,

  /* TODO: Add more token types */
  ZUO = 8,//buyong
  YOU = 9,//buyong
  NOTEQ = 5,
  AND = 6,
  OR = 7,
  LESSEQ = 10,
  HEX = 3,
  RESGISTER = 2,
  NUM = 1,

};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
  {"\\-", '-'},
  {"\\*", '*'},
  {"\\/", '/'},
  {"\\(", ZUO},
  {"\\)", YOU},
  {"\\=\\=", TK_EQ},        // equal
  {"\\!\\=", NOTEQ},
  {"\\&\\&", AND},
  {"\\|\\|", OR},
  {"\\!", '!'},
  {"\\<\\=", LESSEQ},
  {"0[xX][0-9a-fA-F]+", HEX},
  {"\\$[a-zA-Z]*[0-9]*", RESGISTER},
  {"[0-9]*", NUM},
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      printf("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        if (rules[i].token_type == 256) break;
        switch (rules[i].token_type) {
          case '+':
            tokens[nr_token].type = '+';
            strcpy(tokens[nr_token].str, "+");
            nr_token ++;
            break;
          case '-':
            tokens[nr_token].type = '-';
            strcpy(tokens[nr_token].str, "-");
            nr_token ++;
            break;
          case '*':
            tokens[nr_token].type = '*';
            strcpy(tokens[nr_token].str, "*");
            nr_token ++;
            break;
          case '/':
            tokens[nr_token].type = '/';
            strcpy(tokens[nr_token].str, "/");
            nr_token ++;
            break;
          case 8:
            tokens[nr_token].type = '(';
            strcpy(tokens[nr_token].str, "(");
            nr_token ++;
            break;
          case 9:
            tokens[nr_token].type = ')';
            strcpy(tokens[nr_token].str, ")");
            nr_token ++;
            break;
          case 3:
            tokens[nr_token].type = 3;
            strncpy(tokens[nr_token].str, &e[position - substr_len], substr_len);
            nr_token ++;
            break;
          case 2:
            tokens[nr_token].type = 2;
            strncpy(tokens[nr_token].str, &e[position - substr_len + 1], substr_len - 1);
            nr_token ++;
            break;
          case 1:
            tokens[nr_token].type = 1;
            strncpy(tokens[nr_token].str, &e[position - substr_len], substr_len);
            //printf("%d\n", atoi(tokens[nr_token].str));
            nr_token ++;
            break;
          case 4:
            tokens[nr_token].type = 4;
            strcpy(tokens[nr_token].str, "==");
            nr_token++;
            break;
          case 5:
            tokens[nr_token].type = 5;
            strcpy(tokens[nr_token].str, "!=");
            nr_token++;
            break;
          case 6:
            tokens[nr_token].type = 6;
            strcpy(tokens[nr_token].str, "&&");
            nr_token++;
            break;
          case 7:
            tokens[nr_token].type = 7;
            strcpy(tokens[nr_token].str, "||");
            nr_token++;
            break;
          case 10:
            tokens[nr_token].type = 10;
            strcpy(tokens[nr_token].str, "<=");
            nr_token++;
            break;
          case '!':
            tokens[nr_token].type = '!';
            strcpy(tokens[nr_token].str, "!");
            nr_token ++;
            break;
          default: //TODO();
            break;
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}

static int check_parentheses(int a, int b){//判断tokens中的括号是否匹配
  if(strcmp(tokens[a].str, "(") != 0 || strcmp(tokens[b].str, ")") != 0) return 0;
  int term = 0;
  int i;
  for(i = a; i <= b; i++){
    if(strcmp(tokens[i].str, "(") == 0) term++;
    if(strcmp(tokens[i].str, ")") == 0) term--;
    if(term < 0) return 0;
    if(term == 0 && i != b) return 0;
  }
  if(term != 0) return 0;
  return 1;
}

uint32_t eval(int p, int q) {
  if (p > q) {
    /* Bad expression */
    assert(0);
  }
  else if (p == q) {
    /* Single token.
     * For now this token should be a number.
     * Return the value of the number.
     */
     return atoi(tokens[p].str);
  }
  else if (check_parentheses(p, q)) {
    /* The expression is surrounded by a matched pair of parentheses.
     * If that is the case, just throw away the parentheses.
     */
    return eval(p + 1, q - 1);
  }
  else {
    int op = 0;//the position of 主运算符 in the token expression;
    bool flag1 = false;
    bool flag2 = false;
    bool flag3 = false;
    bool flag4 = false;
    bool flag5 = false;

        /*
        if(tokens[p].type == '(' && tokens[p+1].type == '(' && tokens[q].type == ')')
        {
        int count1 = 0;
        int count2 = 0;
        for(int i = p + 1; i <=q - 1; i ++)
        {
        if (tokens[i].type == '(') {count1 ++;}
        else if (tokens[i].type == ')') {count2 ++;}
        }
        if (count1 == count2) {return eval(p + 1, q - 1); }
        }
        */
        for(int i = p ; i <= q ; i ++)
        {
            if(tokens[i].type == '(')
            {
                //printf("%d %d\n", p, q);
                int count1 = 1;
                int count2 = 0;
                while(tokens[i].type != ')' || count2 != count1){//忽略括号内的表达式
                    i ++;
                    if (tokens[i].type == '(') {count1 ++;}
                    else if (tokens[i].type == ')') {count2 ++;}
                }
                //printf("%d\n", i);
                //printf("count1=%d\n", count1);
                //printf("count2=%d\n", count2);
            }
            if(tokens[i].type == 6){//优先级从低到高
                flag1 = true;
                op = i;
            }
            if(!flag1 && tokens[i].type == 7){
                flag2 = true;
                op = i;
            }
            if(!flag1 && !flag2 && (tokens[i].type == 4 || tokens[i].type == 5)){
                flag3 = true;
                op = i;
            }
            if(!flag1 && !flag2 && !flag3 && tokens[i].type == 10){
                flag4 = true;
                op = i;
            }
            if(!flag1 && !flag2 && !flag3 && !flag4 && (tokens[i].type == '+' || tokens[i].type == '-')){
                flag5 = true;
                op = i;
                //printf("op=%d\n", op);
            }
            if(!flag1 && !flag2 && !flag3 && !flag4 && !flag5 && (tokens[i].type == '*' || tokens[i].type == '/') ){
                op = i;
            }
        }
        //printf("val1 %d %d\n", p, op - 1);
        uint32_t val1 = (uint32_t) eval(p, op - 1);
        //printf("%d\n", val1);
        //printf("val2 %d %d\n", op + 1, q);
        uint32_t val2 = (uint32_t) eval(op + 1, q);
        //printf("%d\n", val2);

        int op_type = tokens[op].type;
        switch (op_type) {
            case '+':
                //printf("+de=%d\n", val1 + val2);
                return val1 + val2;
            case '-':
                //printf("-de=%d\n", val1 - val2);
                return val1 - val2;
            case '*':
                return val1 * val2;
            case '/':
                if(val2 == 0){ 
                  printf("div-by-zero\n");
                  assert(0);}
                return (uint32_t)((int)val1 / (int)val2);
            case 4:
                return val1 == val2;
            case 5:
                return val1 != val2;
            case 6:
                return val1 && val2;
            case 7:
                return val1 || val2;
            case 10:
                return val1 <= val2;
            default:
                printf("Fuhao error\n");
                assert(0);
        }
    }
}

static void int2char_16(int x, char str[]){
    int term = x;
    int len = 1;
    term = term /10;
    while (term != 0)
    {
      term = term / 10;
      len ++;
    }
    //printf("%d",len);
    memset(str, 0, len);
    sprintf(str, "%d", x);
}

uint32_t expr(char *e, bool *success) {
  memset(tokens, 0, sizeof(tokens));
  if (!make_token(e)) {printf("zheli/n");
    *success = false;
    return 0;
  }

  //寄存器
  for(int i = 0; i < nr_token; i ++)
    {
      if(tokens[i].type == 2)
      {
        //printf("zheli\n");
        bool flag = false;
        uint32_t term = (uint32_t) isa_reg_str2val(tokens[i].str, &flag);
        if(flag){
          int2char_16(term, tokens[i].str);
          //printf("zheli\n");
        }
        else{
          printf("Transfrom error. \n");
          assert(0);
        }
      }
    }

  /* TODO: Insert codes to evaluate the expression. */
  //TODO();
  uint32_t n;
  n = (uint32_t) eval(0, nr_token-1);
  printf("%d\n", n);
  *success = true;

  return n;
}
