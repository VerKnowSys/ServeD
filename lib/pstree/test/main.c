#include <stdio.h>
#include <stdlib.h>

#include "../svd.h"


extern DataStructure* getPsTree();


int main(char* argv, char** args) {

    printf("%s\n", "Processes:");
    printf("%s", getPsTree()[0].processName);
    
    return 0;
}