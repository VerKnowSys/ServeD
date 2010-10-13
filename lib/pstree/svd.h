#ifndef __svd_h__
#define __svd_h__

typedef struct svdDataStructure {
    
    char *processName;
    unsigned int pid;
    struct svdDataStructure* next; // next element in dynamic array
    
} DataStructure;

#endif