#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../svd.h"


extern char* processes(int compress, int sort);



int main(int argv, char** args) {


    char* preset = "";

    preset = malloc(32768 + 1);
    preset = strcpy(preset, processes(1,1));
    printf("\tProcesses sorted without Threads: \n%s\n\n", preset);
    free(preset);    

    preset = malloc(32768 + 1);
    preset = strcpy(preset, processes(1,0));
    printf("\tProcesses sorted with Threads: \n%s\n\n", preset);
    free(preset);

    preset = malloc(32768 + 1);
    preset = strcpy(preset, processes(0,1));
    printf("\tProcesses unsorted without Threads: \n%s\n\n", preset);
    free(preset);    

    preset = malloc(32768 + 1);
    preset = strcpy(preset, processes(0,0));
    printf("\tProcesses unsorted with Threads: \n%s\n\n", preset);
    free(preset);


    // 2010-10-24 02:11:52 - dmilith - NOTE: to be remembered as example of iteration on dynamic structure in ANSI C:
    // DataStructure* iter;
    //     for (iter = data; NULL != iter; iter = iter->next) {
    //         printf("%s - %d\n", iter->processName, iter->pid);
    //     }
    
    return 0;
}