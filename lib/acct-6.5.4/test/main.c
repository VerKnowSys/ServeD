#include <stdio.h>
#include <sys/time.h>


extern char* sa_svd(int argc, char** argv);
extern char* sa_svd2(int argc, char** argv);

int main(int argc, char** argv) {
    
    int indexBase = 10, outerIndex = 10;
    long mtime, seconds, useconds;
    struct timeval startTime, endTime;
    
            gettimeofday(&startTime, NULL);
            // char* z = sa_svd(2, (char**)"NULL");
            
            char* x[] = {"-m", "-a", "-p"};
            int elemNum = sizeof(x) / sizeof(x[0]);
            printf("NUM: %d, Params: %s\n", elemNum, *x);
            char* z2 = sa_svd2(elemNum, x);
            
            
            fprintf(stdout, "O: %s\n", z2);
            
            gettimeofday(&endTime, NULL);
            
            seconds  = endTime.tv_sec  - startTime.tv_sec;
            useconds = endTime.tv_usec - startTime.tv_usec;
            mtime = ((seconds) * 1000 + useconds/1000.0) + 0.5;
            
            fprintf(stderr, "TEST TIME RESULT. For (4 * %d) => %d calls): %ldms\n", indexBase, (indexBase * 4), mtime);
                
        

    return 0;
}
