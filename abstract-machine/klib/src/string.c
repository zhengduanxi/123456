#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

/*size_t strlen(const char *s) {
  panic("Not implemented");
}

char *strcpy(char *dst, const char *src) {
  panic("Not implemented");
}

char *strncpy(char *dst, const char *src, size_t n) {
  panic("Not implemented");
}

char *strcat(char *dst, const char *src) {
  panic("Not implemented");
}

int strcmp(const char *s1, const char *s2) {
  panic("Not implemented");
}

int strncmp(const char *s1, const char *s2, size_t n) {
  panic("Not implemented");
}

void *memset(void *s, int c, size_t n) {
  panic("Not implemented");
}

void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *out, const void *in, size_t n) {
  panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) {
  panic("Not implemented");
}*/

//计算一个字符串的长度
size_t strlen(const char *s) {
   if (s == NULL) {
    return 0;
  }
  size_t n = 0;
  while(s[n] != '\0') {
    ++n;
  }
  return n;
 
}
 
//将一个字符串复制到另一个字符串中
char *strcpy(char *dst, const char *src) {
  //panic("Not implemented");
  assert(dst != NULL && src != NULL);

  char *tmp = dst;

  while(*src != '\0')
  {
    *tmp++ = *src++;
  }
  *tmp = '\0';
  
  return dst;
}
 
//将源字符串的一部分复制到目标字符串中
char *strncpy(char *dst, const char *src, size_t n) {
  if (src == NULL || dst == NULL) {
    return dst;
  }
  char *ans = dst;
  while (*src != '\0' && n != 0) {
    *dst = *src;
    ++dst;
    ++src;
    --n;
  }
  // 将额外的空字符写入dest，直到写入了n个字符的总数。
  while (n != 0) {
    *dst = '\0';
    ++dst;
    --n;
  }
  return ans;
}
 
//用于将一个字符串追加到另一个字符串的末尾
char *strcat(char *dst, const char *src) {
  //panic("Not implemented");
  assert(dst != NULL && src != NULL);

  char *tmp = dst;
  
  while(*tmp != '\0') tmp++;//使用 while 循环找到 dst 字符串的结尾

  while(*src != '\0') *tmp++ = *src++;

  *tmp = '\0';

  return dst;
}
 
//用于比较两个字符串的内容
int strcmp(const char *s1, const char *s2) {
   size_t i = 0;
  while(s1[i] != '\0' && s2[i] != '\0')
  {
	  if(s1[i] > s2[i])
		  return 1;
	  if(s1[i] < s2[i])
		  return -1;
	  i++;
  }
  if(s1[i] != '\0' && s2[i] == '\0')
	  return 1;
  if(s1[i] == '\0' && s2[i] != '\0')
	  return -1;
  return 0;
 
}
 
//比较两个字符串的前几个字符的内容
int strncmp(const char *s1, const char *s2, size_t n) {
  while(n--)
	{
		if(*s1 > *s2)
			return 1;
		if(*s1 < *s2)
			return -1;
		s1++;
		s2++;
	}
	return 0;
 
}
 
//将指定的内存区域的每个字节都设置为特定的值
void *memset(void *s, int c, size_t n) {
  char *ch = (char *) s;
  while(n-- > 0)
	  *ch++ = c;
  return s;
 
}
 
//用于在内存中移动一块数据。与 memcpy 函数不同，memmove 能够处理源内存区域与目标内存区域重叠的情况，以确保数据正确移动
void *memmove(void *dst, const void *src, size_t n) {
   if(dst < src)
  {
	  char *d = (char *) dst;
	  char *s = (char *) src;
	  while(n--)
	  {
		  *d = *s;
		  d++;
		  s++;
	  }
  }
  else
  {
	  char *d = (char *) dst + n - 1;
	  char *s = (char *) src + n - 1;
	  while(n--)
	  {
		  *d = *s;
		  d--;
		  s--;
	  }
  }
  return dst;
 
}
 
//将一个内存区域的内容复制到另一个内存区域
void *memcpy(void *out, const void *in, size_t n) {
  char *d = (char *) out;
  char *s = (char *) in;
  while(n--)
  {
	  *d = *s;
	  d++;
	  s++;
  }
  return out;
 
}
 
//用于比较两个内存区域的内容
int memcmp(const void *s1, const void *s2, size_t n) {
  if (s1 == NULL || s2 == NULL) {
    return 0;
  }
  const unsigned char *src1 = s1;
  const unsigned char *src2 = s2;
  while (n != 0 && *src1 != '\0' && *src2 != '\0' && *src1 == *src2) {
    --n;
    ++src1;
    ++src2;
  }
  return *src1 == *src2 || n == 0 ? 0 : *src1 < *src2 ? -1 : 1;
}
 
#endif
