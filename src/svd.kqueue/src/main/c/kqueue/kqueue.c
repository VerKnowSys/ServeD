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

kevent_t * kqueue_check(kevent_t * change){
	int nev;
	kevent_t * event;
	
	event = (kevent_t *)malloc(sizeof(kevent_t));
	nev = kevent(kq, change, 1, event, 1, NULL);
	
	if(nev > 0){
		return event;
	} else {
		if(nev == -1) perror("kevent");
		free(event);
		return NULL;
	}
}

kevent_t * kqueue_watch(char * path){
	int f;
	kevent_t * change;
	change = (kevent_t *)malloc(sizeof(kevent_t));
		
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
