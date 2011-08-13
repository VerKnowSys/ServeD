/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"

extern string currentDir();
extern void defaultSignalHandler(int sig);
extern bool fileExists(string strFilename);
extern void spawnBackgroundTask(string abs_java_bin_path, string main_starting_class_param, string cmdline_param, string lockFileName);


int main(int argc, char const *argv[]) {
    
    cout << endl << "ServeD KickStart v" << APP_VERSION << " - " << COPYRIGHT << endl;
        
    signal(SIGINT, defaultSignalHandler);
    signal(SIGQUIT, defaultSignalHandler);
    signal(SIGTERM, defaultSignalHandler);
    
    if (getuid() != 0) {
        cerr << "Kick requires root privileges to run." << endl;
        exit(NOROOT_PRIVLEGES_ERROR);
    }
    
    #ifndef DEVEL
        if (!fileExists(currentDir() + JAR_FILE)) {
            cerr << "No ServeD Core available! Rebuild svd.core first!" << endl;
            exit(CLASSPATH_DIR_MISSING_ERROR);
        }
    #endif

    /* check for home prefix dir */
    if (!fileExists(USERS_HOME_DIR)) {
        cerr << USERS_HOME_DIR << " does not exists. Creating default dir." << endl;
        mkdir(USERS_HOME_DIR, S_IRWXU);
    }

    /* check and create home dir if necessary */
    string arg, homeDir;
    if (argc == 1) {
        arg = "0"; /* NOTE: root will always has uid 0 */
        homeDir = CORE_HOMEDIR;
        #ifdef DEVEL
            cerr << "Spawning ServeD Core" << endl;
            cerr << "HomeDir: " << homeDir << " and argument: " << arg << endl;
            string log = homeDir + "/boot-" + INTERNAL_LOG_FILE; // XXX: hardcoded "boot"
            cerr << "Development mode. Cleaning log: " << log << endl;
            remove(log.c_str());
        #endif
    } else {
        arg = string(argv[1]);
        homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
        #ifdef DEVEL
            cerr << "Spawning ServeD Controller for UID: " << arg << endl;
            cerr << "HomeDir: " << homeDir << " and argument: " << arg << endl;
            string log = homeDir + "/" + arg + "-" + INTERNAL_LOG_FILE;
            cerr << "Development mode. Cleaning log: " << log << endl;
            remove(log.c_str());
        #endif
    }
        
    if (!fileExists(homeDir)) {
        cerr << "Directory: " << homeDir << " does not exists. Creating it." << endl;
        #ifdef DEVEL
                mkdir(homeDir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        #else
                mkdir(homeDir.c_str(), S_IRWXU);
        #endif
        chown(homeDir.c_str(), atoi(arg.c_str()), DEFAULT_USER_GROUP);
    }

    pid_t pid;
    uid_t uid = atoi(arg.c_str());
    string lockName = homeDir + "/" + arg + ".pid";
    ifstream ifs(lockName.c_str(), ios::in);
    ifs >> pid;
    ifs.close();
    
    chdir(homeDir.c_str());
    
    if (uid == 0) {
        spawnBackgroundTask(DEFAULT_JAVA_BIN, SVD_PARAM_ROOT, string(CORE_SVD_ID), lockName);
    } else if (setuid(uid) != 0) {
        cerr << "SetUID(" << uid << ") failed. Aborting." << endl;
        exit(SETUID_ERROR);
    }
    spawnBackgroundTask(DEFAULT_JAVA_BIN, SVD_PARAM_USER, arg, lockName);
    return 0;
}
