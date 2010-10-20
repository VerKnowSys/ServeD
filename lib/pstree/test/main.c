#include <stdio.h>
#include <stdlib.h>

#include "../svd.h"


extern char* processes(int compress, int sort);


int main(int argv, char** args) {

    printf("%s\n", "Processes:");

    char* data = processes(1,1);
    printf("%s", data);
    
    // DataStructure* iter;
    //     for (iter = data; NULL != iter; iter = iter->next) {
    //         printf("%s - %d\n", iter->processName, iter->pid);
    //     }
    
    return 0;
}