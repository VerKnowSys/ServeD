#include <sys/time.h> 
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include "kqueue.h"

int kq;

int kqueue_init(){
	kq = kqueue();
	if(kq == -1){
		perror("kqueue");
		return -1;
	}
	return 0;
}

void kqueue_close(){
	close(kq);
}

struct kevent * kqueue_check(struct kevent * change){
	int nev;
	struct kevent * event;
	
	event = (struct kevent *)malloc(sizeof(struct kevent));
	nev = kevent(kq, change, 1, event, 1, NULL);
	
	if(nev > 0){
		return event;
	} else {
		if(nev == -1) perror("kevent");
		free(event);
		return NULL;
	}
}

struct kevent * kqueue_watch(char * path){
	int f;
	struct kevent * change;
	change = (struct kevent *)malloc(sizeof(struct kevent));
		
	f = open(path, O_RDONLY);
	if(f == -1){
		// No such file or directory!
		perror("open");
		free(kevent);
		return NULL;
	}
	
	// Set event
	EV_SET(change, f, EVFILT_VNODE, EV_ADD | EV_ENABLE | EV_ONESHOT, 
		NOTE_DELETE | NOTE_WRITE | NOTE_EXTEND | NOTE_ATTRIB | NOTE_LINK | NOTE_RENAME | NOTE_REVOKE, 0, 0);
	return change;
}
