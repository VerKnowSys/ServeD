#include <stdio.h>
#include <stdlib.h>
#include <signal.h>


/**

@author dmilith

*/


int main (int argc, char const *argv[]) {
    
    printf("argc: %d\n", argc);
    
    const char* arglist = argv[argc - 1];
    
    
    if (arglist != NULL) {
        int arg = atoi(arglist);
        printf("argv: %d\n", arg);
        raise(arg);
    }
    
    return 0;
    
}