/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include "core.h"

extern "C" {

    /**
     *  @author dmilith
     *
     *   This function is a temporary proxy to get owner of given file
     */
    int getOwner(char* path) {
        struct stat st;
        if (stat(path, &st) == 0) {
            return st.st_uid;
        } else {
            return -1;
        }
    }
    
}
