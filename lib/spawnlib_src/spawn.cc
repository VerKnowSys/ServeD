/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


int main(int argc, char const *argv[]) {

    uid_t uid = getuid();
    if (uid != 0) {
        cout << "Spawn requires root privileges to run." << endl;
        exit(1);
    }

    if (!fileExists(currentDir() + JAR_FILE)) {
        cout << "No ServeD Core available! Rebuild svd.core first!" << endl;
        exit(1);
    }

    if (fileExists(LOCK_FILE)) {
        ifstream ifs(LOCK_FILE, ios::in);
        pid_t pid;
        ifs >> pid;
        if (processAlive(pid)) {
            cout << "Process still alive for core svd (pid: " << pid << "). Aborting." << endl;
            exit(1);
        } else { /* process isn't alive but socket file still exists? */
            if (fileExists(SOCK_FILE)) {
                log_message("Removing socket file (process is dead but file is still there).");
                string rmCmd = "/bin/rm " + string(SOCK_FILE);
        		system(rmCmd.c_str());
            }
        }
    }
    
    stringstream ret;
    ret << "Spawn uid: " << uid << endl;
    
    log_message(ret.str());
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.boot", "boot", true, LOCK_FILE);
    
    return 0;
}
