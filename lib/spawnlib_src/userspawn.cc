/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"


int main(int argc, char const *argv[]) {

    if (!fileExists(currentDir() + JAR_FILE)) {
        cout << "No ServeD Core available! Rebuild svd.core first!" << endl;
        exit(1);
    }
    
    if (argc == 1) {
        cout << "First argument must be uid of user to run ServeD userspace" << endl;
        exit(1);
    }
    
    /* hacky way to remove old lock file */
    string lockName = string(argv[1]) + "-" + string(LOCK_FILE);
    string rmCmd = "/bin/rm " + lockName;
    system(rmCmd.c_str());

    uid_t uid = atoi(argv[1]);
    // setsid(); /* obtain a new process group */
    setuid(uid);
    stringstream    ret;
    ret << "Spawned with uid: " << getuid() << endl;
    ret << "Given param uid: " << uid << endl;
    log_message(ret.str());
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.userboot", string(argv[1]), false, lockName);
    
    return 0;
}
