#ifndef _KQUEUE_H_
#define _KQUEUE_H_

#include <sys/event.h>

int kqueue_init();
void kqueue_close();
struct kevent * kqueue_check(struct kevent * change);
struct kevent * kqueue_watch(char * path);

#endif
