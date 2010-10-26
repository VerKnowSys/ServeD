#include <stdio.h>
#include <sys/time.h>
// #include "../acct_served.c"


/* unsigned int BUFFER = 512; 2010-10-24 14:52:09 - dmilith - NOTE: HACK: additional buffer bytes, for process list. Should be enough, but it's still a hack */
/* #define operation (index % 10) */

extern char* sa_svd(int argc, char** argv);
extern char* sa_svd2(int argc, char** argv);

int main(int argc, char** argv) {
    
    int indexBase = 10, outerIndex = 10;
    long mtime, seconds, useconds;
    struct timeval startTime, endTime;
    
    /*int debug = 0;
    int indexBase = 10, outerIndex = 10;
    char count = 0;
    char *preset, *tmp;
    int indx, index, baseindx;
    long mtime, seconds, useconds, seconds2, useconds2;*/
    /* int i; */
        
    /*if (argc >= 2) {
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
                    preset = malloc((sizeof processes(1, 1)) + BUFFER);
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
    */

        {    
            gettimeofday(&startTime, NULL);
            // char* z = sa_svd(2, (char**)"NULL");
            char* x[] = {"-P", "/var/account/acct", "-m"};
            char* z2 = sa_svd2(3, x);
            
            printf("O: %s\n", z2);
            
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
