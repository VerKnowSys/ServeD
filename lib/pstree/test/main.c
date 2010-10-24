#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>

#include "../svd.h"


extern char* processes(int compress, int sort);



int main(int argv, char** args) {
    
    int debug = 1;
    int indexBase = 25, outerIndex = 25;
    /* int i; */
    
    /* for (i = 0; i < 1; i++) - test loop for instrumentation */
        for (outerIndex = indexBase; outerIndex > 0; --outerIndex) {
            int index = 0;
            struct timeval startTime, endTime;

            gettimeofday(&startTime, NULL);
            for (index = indexBase; index > 0; --index) {
                char* preset = malloc(12000);
                preset = processes(0, 1);
                if (debug) {
                    printf("\n\n\n\tTEST: index: %d", index);
                    printf("\n\n\n\tTEST: Processes sorted without Threads: \n%s\n\n", preset);
                }
                free(preset);

                preset = malloc(12000);
                preset = processes(1, 1);
                if (debug) {
                    printf("\n\n\n\tTEST: index: %d", index);
                    printf("\n\n\n\tTEST: Processes sorted with Threads: \n%s\n\n", preset);
                }
                free(preset);

                preset = malloc(12000);
                preset = processes(0, 0);
                if (debug) {
                    printf("\n\n\n\tTEST: index: %d", index);
                    printf("\n\n\n\tTEST: Processes unsorted without Threads: \n%s\n\n", preset);
                }
                free(preset);

                preset = malloc(12000);
                preset = processes(1, 0);
                if (debug) {
                    printf("\n\n\n\tTEST: index: %d", index);
                    printf("\n\n\n\tTEST: Processes unsorted with Threads: \n%s\n\n", preset);
                }
                free(preset);

            }
            gettimeofday(&endTime, NULL);
            printf("TEST TIME RESULT. For (4 * %d) => %d calls): %dms\n", indexBase, (indexBase * 4),
                ((endTime.tv_usec - startTime.tv_usec)/1000));
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
