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

    string arg = string(argv[1]);
    string lockName = arg + "-" + string(LOCK_FILE);
    
    if (fileExists(lockName)) {
        ifstream ifs(lockName.c_str(), ios::in);
        pid_t pid;
        ifs >> pid;
        if (processAlive(pid)) {
            log_message("Process still alive for uid: " + arg + ". Aborting.");
            exit(1);
        }    
    }
    
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
