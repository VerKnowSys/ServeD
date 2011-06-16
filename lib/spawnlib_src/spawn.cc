/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


void usage(const char* scriptName) {
    cout << "Usage: " << string(scriptName) << " [start | stop]" << endl;
    exit(0);
}


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

    /* stop & start action */
    if (argc != 1) {
        string arg = string(argv[1]);
        if (arg == "stop") {
            /* kill pid under LOCK_FILE */
            ifstream ifs(LOCK_FILE, ios::in);
            pid_t pid;
            ifs >> pid;
            kill(pid, SIGTERM);
            cout << "Spawn stopped." << endl;
            ifs.close();
            /* also kill socket server process with pid in SOCKET_LOCK_FILE */
            ifstream ifs1(SOCKET_LOCK_FILE, ios::in);
            ifs1 >> pid;
            kill(pid, SIGTERM);
            cout << "Spawn Socket-Server stopped." << endl;
            ifs1.close();
            performCleanup();
            exit(0);
        } else
            if (arg != "start") usage(argv[0]);
    } else
        usage(argv[0]);
    
    if (fileExists(LOCK_FILE)) {
        ifstream ifs(LOCK_FILE, ios::in);
        pid_t pid;
        ifs >> pid;
        if (processAlive(pid)) {
            cout << "Process still alive for core svd (pid: " << pid << "). Aborting." << endl;
            exit(1);
        } else { /* process isn't alive but socket/lock file still exist? */
            performCleanup();
        }
        ifs.close();
    }
    
    stringstream ret;
    ret << "Spawn uid: " << uid << endl;
    
    log_message(ret.str());
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.boot", string(CORE_SVD_ID), true, LOCK_FILE);
    
    return 0;
}
