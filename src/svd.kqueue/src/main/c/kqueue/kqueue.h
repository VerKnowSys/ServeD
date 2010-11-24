#ifndef _KQUEUE_H_
#define _KQUEUE_H_

#include <sys/event.h>

typedef struct kevent kevent_t;

int kqueue_init();
void kqueue_close();
kevent_t * kqueue_check(kevent_t * change);
kevent_t * kqueue_watch(char * path);

#endif
