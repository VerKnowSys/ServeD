/*
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "core.h"

extern string currentDir();
extern void defaultSignalHandler(int sig);
extern bool fileExists(string strFilename);
extern void spawnBackgroundTask(execParams, string lockFileName);


int main(int argc, char const *argv[]) {

    cout << "ServeD KickStart v" << APP_VERSION << " - " << COPYRIGHT << endl;

    signal(SIGINT, defaultSignalHandler);
    signal(SIGQUIT, defaultSignalHandler);
    signal(SIGTERM, defaultSignalHandler);

    if (getuid() != 0) {
        cerr << "Kick requires root privileges to run." << endl;
        exit(NOROOT_PRIVLEGES_ERROR);
    }

    #ifndef DEVEL
        if (!fileExists(USER_JAR_FILE) or !fileExists(ROOT_JAR_FILE)) {
            cerr << "No ServeD Core available in system! Install ServeD Core first!" << endl;
            exit(INSTALLATION_MISSING_ERROR);
        }
    #endif

    /* check for home prefix dir */
    if (!fileExists(USERS_HOME_DIR)) {
        cerr << USERS_HOME_DIR << " does not exists. Creating default dir." << endl;
        #ifdef DEVEL
            mkdir(USERS_HOME_DIR, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        #else
            mkdir(USERS_HOME_DIR, S_IRWXU | S_IXGRP | S_IXOTH);
        #endif
    }

    /* check and create home dir if necessary */
    string homeDir;
    execParams params;
    if (argc == 1) {
        params.svdArg = "0"; /* NOTE: root will always has uid 0 */
        homeDir = CORE_HOMEDIR;
        #ifdef DEVEL
            cerr << "Spawning ServeD Core" << endl;
            cerr << "HomeDir: " << homeDir << " and argument: " << params.svdArg << endl;
            string log = homeDir + CORE_SVD_ID + "-" + INTERNAL_LOG_FILE;
            cerr << "Development mode. Cleaning log: " << log << endl;
            remove(log.c_str());
        #endif
    } else {
        params.svdArg = string(argv[1]);
        homeDir = string(USERS_HOME_DIR) + params.svdArg + "/"; /* NOTE: /Users/$UID homedir format */
        #ifdef DEVEL
            cerr << "Spawning ServeD Controller for UID: " << params.svdArg << endl;
            cerr << "HomeDir: " << homeDir << " and argument: " << params.svdArg << endl;
            string log = homeDir + params.svdArg + "-" + INTERNAL_LOG_FILE;
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
        chown(homeDir.c_str(), atoi(params.svdArg.c_str()), DEFAULT_USER_GROUP);
    }

    pid_t pid;
    uid_t uid = atoi(params.svdArg.c_str());
    string lockName = homeDir + params.svdArg + ".pid";
    ifstream ifs(lockName.c_str(), ios::in);
    ifs >> pid;
    ifs.close();

    chdir(homeDir.c_str());

    params.javaPath = DEFAULT_JAVA_BIN;

    if (uid == 0) {
        params.svdArg = string(CORE_SVD_ID);
        #ifdef DEVEL
            params.mainClass = ROOT_MAIN_CLASS;
            params.classPathFile = ROOT_CLASSPATH_FILE;
        #endif
        spawnBackgroundTask(params, lockName);
    } else if (setuid(uid) != 0) {
        cerr << "SetUID(" << uid << ") failed. Aborting." << endl;
        exit(SETUID_ERROR);
    }

    #ifdef DEVEL
        params.mainClass = USER_MAIN_CLASS;
        params.classPathFile = USER_CLASSPATH_FILE;
    #endif

    spawnBackgroundTask(params, lockName);
    return 0;
}
