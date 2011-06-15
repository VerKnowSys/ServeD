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
    
    uid_t uid = getuid();
    stringstream ret;
    ret << "Spawn uid: " << uid << endl;
    
    if (uid != 0) {
        cout << "Spawn requires root privileges to run." << endl;
        exit(1);
    }
    
    log_message(ret.str());
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.boot", "boot", true, LOCK_FILE);
    
    return 0;
}
