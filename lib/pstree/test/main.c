#include <stdio.h>
#include <stdlib.h>
#include "../pstree.c"


int main(char* argv, char** args) {

    printf("%s", "dupa");
    printf("%s", getPsTree()[0].processName);
    
    return 0;
}