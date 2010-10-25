#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>

#include "../svd.h"


extern char* processes(int compress, int sort);


unsigned int BUFFER = 512; /* 2010-10-24 14:52:09 - dmilith - NOTE: HACK: additional buffer bytes, for process list. Should be enough, but it's still a hack */
#define operation (index % 25)


int main(int argc, char** argv) {
    
    int debug = 0;
    int indexBase = 25, outerIndex = 25;
    char count = 0;
    int indx;
    int baseindx;
    struct timeval startTime, endTime, startTime2, endTime2;
    long mtime, seconds, useconds, seconds2, useconds2;
    /* int i; */
        
    if (argc >= 2) {
        for (count = 1; count < argc; count++) {
            if (strcmp(argv[count], "quick") == 0) {
                if (debug) {
                    printf("\n\tTEST: in Quick mode.");
                    printf("\n\tTEST: argv[%d] = %s\n", count, argv[count]);                    
                }
                
                indx = 100;
                baseindx = 100;
                gettimeofday(&startTime, NULL);
                printf("TEST TIME RESULT FOR SINGLE RUN:\n");
                while (indx > 0) {
                    gettimeofday(&startTime2, NULL);
                    char* preset = malloc((sizeof processes(1, 1)) + BUFFER);
                    preset = processes(1, 1);
                    gettimeofday(&endTime2, NULL);
                    free(preset);
                    
                    if (debug)
                        printf("\n\tTEST: Sorted processes with Threads:\n%s\n", preset);

                    seconds2  = endTime2.tv_sec  - startTime2.tv_sec;
                    useconds2 = endTime2.tv_usec - startTime2.tv_usec;
                    mtime = ((seconds2) * 1000 + useconds2/1000.0) + 0.5;
                    printf("%ldms.. ", mtime);

                    indx--;
                }
                gettimeofday(&endTime, NULL);

                seconds  = endTime.tv_sec  - startTime.tv_sec;
                useconds = endTime.tv_usec - startTime.tv_usec;
                mtime = ((seconds) * 1000 + useconds/1000.0) + 0.5;
                printf("\nTEST TIME RESULT FOR %d RUNS: %ldms\n", baseindx, mtime);
                
                exit(0);
                
            }
        }
    } else {
        if (debug) {
            printf("\n\tTEST: in Normal mode.");
            printf("\n\tTEST: argv[%d] = %s\n", count, argv[count]);                    
        }
    }

    
    /* for (i = 0; i < 1; i++) - test loop for instrumentation */
        for (outerIndex = indexBase; outerIndex > 0; --outerIndex) {
            int index = 0;
            
            gettimeofday(&startTime, NULL);
            for (index = indexBase; index > 0; --index) {
                
                char* tmp = processes(1, 0); /* if 1(showThreads), 0(sort) => object should be biggest of all */
                char* preset = malloc((sizeof tmp) + BUFFER);
                preset = processes(0, 1);
                if (debug && operation) {
                    printf("\n\tTEST: index: %d", index);
                    printf("\n\tTEST: Processes sorted without Threads: \n%s\n", preset);
                }
                free(preset);

                preset = malloc((sizeof tmp) + BUFFER);
                preset = processes(1, 1);
                if (debug && operation) {
                    printf("\n\tTEST: index: %d", index);
                    printf("\n\tTEST: Processes sorted with Threads: \n%s\n", preset);
                }
                free(preset);

                preset = malloc((sizeof tmp) + BUFFER);
                preset = processes(0, 0);
                if (debug && operation) {
                    printf("\n\nTEST: index: %d", index);
                    printf("\n\nTEST: Processes unsorted without Threads: \n%s\n", preset);
                }
                free(preset);

                preset = malloc((sizeof tmp) + BUFFER);
                preset = processes(1, 0);
                if (debug && operation) {
                    printf("\n\tTEST: index: %d", index);
                    printf("\n\tTEST: Processes unsorted with Threads: \n%s\n", preset);
                }
                free(preset);

            }
            gettimeofday(&endTime, NULL);
            
            seconds  = endTime.tv_sec  - startTime.tv_sec;
            useconds = endTime.tv_usec - startTime.tv_usec;
            mtime = ((seconds) * 1000 + useconds/1000.0) + 0.5;
            
            printf("TEST TIME RESULT. For (4 * %d) => %d calls): %ldms\n", indexBase, (indexBase * 4), mtime);
                
        }
        

    /*
    2010-10-24 02:11:52 - dmilith - NOTE: to be remembered as example of iteration on dynamic structure in ANSI C:
    
    DataStructure* iter;
        for (iter = data; NULL != iter; iter = iter->next) {
            printf("%s - %d\n", iter->processName, iter->pid);
        }
    */
    
    return 0;
}
