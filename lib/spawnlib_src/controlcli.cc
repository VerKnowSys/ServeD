/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


int main(int argc, char *argv[]) {
    
    if (argc == 1) {
        cout << "No message to send? (first param)" << endl;
        exit(1);
    }
    sendSpawnMessage(argv[1]);
    return 0;
}
