#include <stdio.h>
#include "kqueue.h"

int main(int argc, char ** args){
	struct kevent * watch;
	struct kevent * event;
	
	kqueue_init();
	
	watch = kqueue_watch("/tmp/xx");
	for(;;){
		event = kqueue_check(watch);
		if(event != NULL) printf("fflags = %d\n", event->fflags);
		// break if DELETED
		// if(event->fflags & NOTE_DELETE)	printf(", NOTE_DELETE");
		// if(event->fflags & NOTE_WRITE)	printf(", NOTE_WRITE");
		// if(event->fflags & NOTE_EXTEND)	printf(", NOTE_EXTEND");
		// if(event->fflags & NOTE_ATTRIB) 	printf(", NOTE_ATTRIB");
		// if(event->fflags & NOTE_LINK) 	printf(", NOTE_LINK");
		// if(event->fflags & NOTE_RENAME) 	printf(", NOTE_RENAME");
		// if(event->fflags & NOTE_REVOKE) 	printf(", NOTE_REVOKE");
		// printf("\n");
		// 
		// if(event->fflags & NOTE_DELETE){
		// 	printf("    ! File deleted\n");
		// 	return; // TODO: Reset watcher?
		// }
		// 
		// if(event->fflags & NOTE_EXTEND || event->fflags & NOTE_WRITE){
		// 	printf("    ! File modified\n");
		// }
		// 
		// if(event->fflags & NOTE_ATTRIB){
		// 	printf("    ! File attributes modified\n");
		// }
	}	
	
	kqueue_close();
	return 0;
}



// K data = kquque_watch(path);
// while(true){
// 	int res = kqueue_check(data);
// 	if(res ...){
// 		...
// 	}
// }