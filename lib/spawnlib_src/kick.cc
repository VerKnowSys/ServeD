/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


using namespace std;


int main(int argc, char const *argv[]) {

    if (getuid() != 0) {
        cerr << "Kick requires root privileges to run." << endl;
        exit(1);
    }
    
#ifndef DEVEL
    if (!fileExists(currentDir() + JAR_FILE)) {
        cerr << "No ServeD Core available! Rebuild svd.core first!" << endl;
        exit(1);
    }
#endif

    /* check for home prefix dir */
    if (!fileExists(USERS_HOME_DIR)) {
        cerr << USERS_HOME_DIR << " does not exists. Creating default dir." << endl;
        mkdir(USERS_HOME_DIR, 0750); /* XXX: hardcoded permissions but it's safe */
    }

    /* check and create home dir if necessary */
    string arg, homeDir;
    if (argc == 1) {
        cerr << "Spawning core" << endl;
        arg = "0";
        homeDir = CORE_HOMEDIR; 
    } else {
        arg = string(argv[1]);
        homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
    }
    
    
#ifdef DEVEL
    cerr << "HomeDir: " << homeDir << " and argument: " << arg << endl;
    /* also remove contents of home directory before each running in devel mode */
    // if (homeDir != string(USERS_HOME_DIR)) {
        // cerr << "Cleaning files in homeDir: " << homeDir << endl;
        // spawn("/bin/rm -rf " + homeDir);
    // }
#endif
    if (!fileExists(homeDir)) {
        cerr << homeDir << " does not exists. Creating it." << endl;
#ifdef DEVEL
        mkdir(homeDir.c_str(), 0755); /* XXX: hardcoded permissions but it's safe */
#else
        mkdir(homeDir.c_str(), 0711); /* XXX: hardcoded permissions but it's safe */
#endif
        chown(homeDir.c_str(), atoi(arg.c_str()), 20); /* XXX: hardcoded "staff" group */
    }

    pid_t pid;
    uid_t uid = atoi(arg.c_str());
    string lockName = homeDir + "/" + arg;
    ifstream ifs(lockName.c_str(), ios::in);
    ifs >> pid;
    ifs.close();

    chdir(homeDir.c_str());
    cerr << "Starting UserSpawn for uid: " << uid << " in homeDir: " << homeDir << endl;
    
    if (uid == 0) {
        // spawn core
        spawnBackgroundTask("/usr/bin/java", "svd", string(CORE_SVD_ID), LOCK_FILE);
    } else
    if (setuid(uid) != 0) {
        cerr << "SetUID(" << uid << ") failed. Aborting." << endl;
        exit(1);
    }
    spawnBackgroundTask("/usr/bin/java", "user", arg, lockName);

    return 0;
}
