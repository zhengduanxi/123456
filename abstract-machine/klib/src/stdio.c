#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

/*int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
  panic("Not implemented");
}*/

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

static char *number(char *str, int num, int base)
{
  int tmp[100], i = 0;
  if(num < 0)
  {
    *str++ = '-';
    num = -num;
  }
  while(num)
  {
    tmp[i++] = num % base;
    num /= base;
  }
  
  i--;
  
  while(i >= 0) *str++ = tmp[i--] + '0';   //注意要转化为字符型数字
  
  return str;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  //panic("Not implemented");
  char *str = out;
  const char *s;
  int base;   
  int num;

  for(; *fmt != '\0'; ++fmt)
  {
    if(*fmt != '%')
    {
      *str++ = *fmt;
      continue;
    }

    //*fmt == '%'
    ++fmt;

    base = 10;  //默认为10进制

    switch(*fmt)
    {
      case 'c':
        *str++ = (unsigned char) va_arg(ap, int);
        continue;
      case 's':
        s = va_arg(ap, char *);

        /****    Plan A    ****/

        //for(int i = 0; s[i]; i++)
        //  *str++ = s[i];

        /****    Plan B    ****/
        strcat(str, s);
        str += strlen(s);
        continue;
      case 'd':
        break;      //用于跳出switch语句
      default:
        *str++ = '%';
        if(*fmt)
          *str++ = *fmt;
        else
          --fmt;    //防止溢出
        continue;
    }
    num = va_arg(ap, int);

    str = number(str, num, base);
  }
  *str = '\0';
  return str - out;  //返回写入的字符数
}

/*
stdarg中部分宏的解释
va_list 是用于存放参数列表的数据结构。
va_start 函数根据初始化last来初始化参数列表,last为固定参数中的最后一个固定参数.(原因在后面的宏解析中)
va_arg 函数用于从参数列表中取出一个参数，参数类型由type指定。
va_copy 函数用于复制参数列表。
va_end 函数执行清理参数列表的工作。
*/
int sprintf(char *out, const char *fmt, ...) {
  //panic("Not implemented");
  assert(out);

  va_list args;
  int i;

  va_start(args, fmt);
  i = vsprintf(out, fmt, args);
  va_end(args);

  return i;
}

/*static char sprint_buf[1024];
//可变函数在内部实现的过程中是从右向左压入堆栈，从而保证了可变参数的第一个参数始终位于栈顶/
int printf(const char *fmt, ...) {//可以有一个或多个固定参数
  va_list args; //用于存放参数列表的数据结构
  int n;
  //根据最后一个fmt来初始化参数列表，至于为什么是最后一个参数，是与va_start有关。/
  va_start(args, fmt);
  n = vsprintf(sprint_buf, fmt, args);
  va_end(args);//执行清理参数列表的工作
  putstr(sprint_buf);
  return n;
}*/

int printf(const char *fmt, ...) {
  //panic("Not implemented");
  int i;
  char buf[256];

  memset(buf, 0, sizeof(buf));

  //va_list arg = (va_list)((char*)(&fmt) + 4);

  va_list args;
  va_start(args, fmt);
  i = vsprintf(buf, fmt, args);
  va_end(args);

  char *tmp = buf;

  while(*tmp != 0)
  {
    putch(*tmp);
    tmp++;
  }

  return i;
}


#endif
