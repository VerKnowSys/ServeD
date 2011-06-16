/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


using namespace std;


int main(int argc, char const *argv[]) {
    
    if (!fileExists(currentDir() + JAR_FILE)) {
        cout << "No ServeD Core available! Rebuild svd.core first!" << endl;
        exit(1);
    }
    
    if (argc == 1) {
        cout << "First argument must be uid of user to run ServeD userspace" << endl;
        exit(1);
    }

    /* check for home prefix dir */
    if (!fileExists(USERS_HOME_DIR)) {
        stringstream lm;
        lm << USERS_HOME_DIR << " does not exists. Creating default dir." << endl;
        log_message(lm.str());
        mkdir(USERS_HOME_DIR, 0755); /* XXX: hardcoded permissions but it's safe */
    }
    
    /* check and create home dir if necessary */
    string arg = string(argv[1]);
    string homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
    if (!fileExists(homeDir)) {
        stringstream lm;
        lm << homeDir << " does not exists. Creating it." << endl;
        log_message(lm.str());
        mkdir(homeDir.c_str(), 0711); /* XXX: hardcoded permissions but it's safe */
        chown(homeDir.c_str(), atoi(arg.c_str()), 20); /* XXX: hardcoded "staff" group */
    }

    chdir(homeDir.c_str());
    string lockName = homeDir + "/" + string(LOCK_FILE);
    
    if (fileExists(lockName)) {
        ifstream ifs(lockName.c_str(), ios::in);
        pid_t pid;
        ifs >> pid;
        if (processAlive(pid)) {
            log_message("Process still alive for uid: " + arg + ". Aborting.");
            exit(1);
        } else { /* process isn't alive but socket/lock file still exist? */
            if (fileExists(lockName)) {
                stringstream msg;
                msg << "Removing userspawn lock file: " << lockName << " (process is dead but file is still there)." << endl;
                log_message(msg.str());
                string rmCmd = "/bin/rm " + string(lockName);
        		system(rmCmd.c_str());
            }
        }
        ifs.close();
    }
    
    uid_t uid = atoi(argv[1]);
    setuid(uid);
    stringstream    ret;
    ret << "Spawned with uid: " << getuid() << endl;
    ret << "Given param uid: " << uid << endl;
    log_message(ret.str());
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.userboot", string(argv[1]), false, lockName);
    
    return 0;
}
