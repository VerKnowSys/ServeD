/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


int main(int argc, char const *argv[]) {
        
    if (getuid() != 0) {
        cerr << "Kick requires root privileges to run." << endl;
        exit(NOROOT_PRIVLEGES_ERROR);
    }
    
    /* check and create home dir if necessary */
    string arg, homeDir;
    if (argc == 1) {
        cerr << "No uid given! Exitting." << endl;
        exit(1);
    } else {
        arg = string(argv[1]);
        if (arg == "0") {
            cerr << "Cannot spawn as root!" << endl;
            exit(1);
        }
        homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
        #ifdef DEVEL
            cerr << "Spawning user shell for UID: " << arg << endl;
        #endif
        
        uid_t uid = atoi(arg.c_str());
        chdir(homeDir.c_str());
        if (setuid(uid) != 0) {
            cerr << "Error setuid to uid: " << uid << endl;
            exit(1);
        }
        system(DEFAULT_SHELL_COMMAND);
    }
    return 0;
}
