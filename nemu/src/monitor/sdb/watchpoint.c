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

#include "sdb.h"

#define NR_WP 32

typedef struct watchpoint {
  int NO;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */

  char expr[100];
  int value;
}WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

WP* new_wp()
{
  if(free_==NULL)
  {
    printf("free_没有空闲监视点\n");
    assert(0);
  }  
  WP *pos=free_;
  free_++;
  pos->next=head;
  head=pos;
  return pos;
}

void free_wp(WP *wp)
{
  if(wp==head)
  {
    head=head->next;
  }
  else
  {
    WP *pos=head;
    while(pos && pos->next!=wp)
    {
      pos++;
    }
    if (!pos) 
    {
      printf("输入的监视点不在head链表中\n");
      assert(0);
    }
    pos->next=wp->next;
  }
  wp->next=free_;
  free_=wp;
}

void sdb_watchpoint_display(){
    WP *pos=head;
  if(!pos)
  {
    printf("NO watchpoints\n");
    return;
  }
   printf("%-8s%-8s\n", "No", "Expression");
  while (pos) {
    printf("%-8d%-8s\n", pos->NO, pos->expr);
    pos = pos->next;
  }
}

void create_watchpoint(char* args){
  WP* wp = new_wp();
  strcpy(wp->expr, args);
  bool success = false;
  wp->value=expr(wp -> expr,&success);
  printf("Watchpoint %d: %s\n", wp->NO, wp->expr);
}

void delete_watchpoint(int no){
  if(no<0 || no>=NR_WP)
  {
    printf("N is not in right\n");
    assert(0);
  }
  WP* wp = &wp_pool[no];
  free_wp(wp);
  printf("Delete watchpoint %d: %s\n", wp->NO, wp->expr);
}

void check_watchpoint(){
  WP* pos = head;
  while (pos) {
    bool term;
    word_t new = expr(pos->expr, &term);
    if (pos->value != new) {
      printf("Watchpoint %d: %s\n"
        "Old value = %d\n"
        "New value = %d\n"
        , pos->NO, pos->expr, pos->value, new);
      pos->value = new;
      nemu_state.state=NEMU_STOP;
    }
    pos = pos->next;
  }
}

