#include <stdio.h>
#include <stdlib.h>

#include "../svd.h"


extern DataStructure* getPsTree();


int main(char* argv, char** args) {

    printf("%s\n", "Processes:");
    int i = 0;
    DataStructure* data = getPsTree();
    
    DataStructure* iter;
    for (iter = data; NULL != iter; iter = iter->next) {
        printf("%s - %d\n", iter->processName, iter->pid);
    }
    
    return 0;
}