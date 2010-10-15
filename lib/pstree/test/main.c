#include <stdio.h>
#include <stdlib.h>

#include "../svd.h"


extern DataStructure* processes();


int main(char* argv, char** args) {

    printf("%s\n", "Processes:");

    DataStructure* data = processes();
    
    DataStructure* iter;
    for (iter = data; NULL != iter; iter = iter->next) {
        printf("%s - %d\n", iter->processName, iter->pid);
    }
    
    return 0;
}