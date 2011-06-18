/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


using namespace std;


int main(int argc, char const *argv[]) {
    
    stringstream lm;
    
#ifndef DEVEL
    if (!fileExists(currentDir() + JAR_FILE)) {
        lm << "No ServeD Core available! Rebuild svd.core first!";
        log_message(lm.str());
        exit(1);
    }
#endif
    
    if (argc == 1) {
        lm << "First argument must be uid of user to run ServeD userspace";
        log_message(lm.str());
        exit(1);
    }

    /* check for home prefix dir */
    if (!fileExists(USERS_HOME_DIR)) {
        lm << USERS_HOME_DIR << " does not exists. Creating default dir.";
        log_message(lm.str());
        mkdir(USERS_HOME_DIR, 0755); /* XXX: hardcoded permissions but it's safe */
    }
    
    /* check and create home dir if necessary */
    string arg = string(argv[1]);
    string homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
    if (!fileExists(homeDir)) {
        lm << homeDir << " does not exists. Creating it.";
        log_message(lm.str());
        mkdir(homeDir.c_str(), 0711); /* XXX: hardcoded permissions but it's safe */
        chown(homeDir.c_str(), atoi(arg.c_str()), 20); /* XXX: hardcoded "staff" group */
    }
    
    string lockName = homeDir + "/" + string(LOCK_FILE);
    pid_t pid;
    
    chdir(homeDir.c_str());

    uid_t uid = atoi(argv[1]);
    
    if (fileExists(lockName)) {
        ifstream ifs(lockName.c_str(), ios::in);
        ifs >> pid;
        if (processAlive(pid)) {
            /* stop action */
            if (argv[2] != NULL) {
                string arg2 = string(argv[2]);
                if (arg2 == "stop") {
                    kill(pid, SIGTERM);
                    lm << "UserSpawn (uid: " << uid << ", pid: " << pid << ") stopped." << endl;
                    log_message(lm.str());
                    
                    /* also remove lock file after stopping service */
                    if (fileExists(lockName)) {
                        lm << "Removing userspawn lock file: " << lockName << " (process is dead but file is still there)." << endl;
                        log_message(lm.str());
                        string rmCmd = "/bin/rm " + string(lockName);
                		system(rmCmd.c_str());
                    }
                    
                    exit(0);
                }
            }
            lm << "Process still alive for uid: " << uid << ". Aborting.";
            log_message(lm.str());
            exit(1);
        } else { /* process isn't alive but socket/lock file still exist? */
            if (fileExists(lockName)) {
                lm << "Removing userspawn lock file: " << lockName << " (process is dead but file is still there)." << endl;
                log_message(lm.str());
                string rmCmd = "/bin/rm " + string(lockName);
        		system(rmCmd.c_str());
            }
        }
        ifs.close();
    }
    
    setuid(uid);
    lm << "Given param uid: " << uid;
    log_message(lm.str());

    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.userboot", string(argv[1]), false, lockName);
    
    return 0;
}