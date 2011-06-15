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
    if (uid != 0) {
        cout << "Respawn requires root privileges to run." << endl;
        exit(1);
    }
    
    spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.boot", "params", true);
    
    return 0;
}
