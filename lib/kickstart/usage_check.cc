#include "core.h"


int main(int argc, char const *argv[]) {

    if (argc == 1) {
        cerr << "No UID argument given!" << endl;
        exit(NO_UID_GIVEN_ERROR);
    }

    getProcessUsage(atoi(argv[1]), true);
    
    
    return 0;
}
