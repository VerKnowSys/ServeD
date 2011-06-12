/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <limits.h>

using namespace std;

extern "C" {

    const int CHILD_EXCEPTION = 250;    
    const int SETUID_EXCEPTION = 251;
    const int POPEN_EXCEPTION = 252;
    const int FORK_EXCEPTION = 253;
    const int EXIT_FAILURE_EXCEPTION = 254;

    /* function prototypes */
    char* spawn(char* _command);

}
