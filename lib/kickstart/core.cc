/*
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include "core.h"


extern string currentDir();
extern string escape(string input);
extern void defaultSignalHandler(int sig);
extern void log_message(string message);

const static string coreDir = currentDir();


    #ifdef DEVEL

        string getClassPath(string classPathFile) {
            string core = coreDir + classPathFile;
            string cp = "";
            ifstream f(core.c_str());
            if (f.is_open()){
                f >> cp;
                f.close();
            } else {
                cerr << "Directory " << core << " not found!" << endl;
                exit(CLASSPATH_DIR_MISSING_ERROR);
            }
            return cp;
        }

    #endif


    void load_svd64(execParams params) {
        string jnalp = "-Djava.library.path=" + string(LIBRARIES_DIR);
        int count = 13;
        #ifdef DEVEL
            count = 14;
        #endif
        char *args[] = {
            (char*)"java",
            (char*)"-d64",
            (char*)"-client",
            (char*)"-Xmn2m",
            (char*)"-XX:NewRatio=1",
            (char*)"-Xms8m",
            (char*)"-Xmx128m",
            (char*)"-XX:+UseCompressedOops",
            (char*)"-Dfile.encoding=UTF-8",
            (char*)jnalp.c_str(),
            #ifndef DEVEL
                /* when not devel, use classes from assembly jar */
                (char*)"-jar",
                (char*)params.jar.c_str(),
            #else
                /* when devel, use classes from compile folders */
                (char*)"-cp",
                (char*)getClassPath(params.classPathFile).c_str(),
                (char*)params.mainClass.c_str(),
            #endif
            (char*)params.svdArg.c_str(),
            (char*)0
        };

        #ifdef DEVEL
            cerr << "Loading svd-64, with opts: [";
            for (int i = 0; i < count; i++) {
                cerr << args[i] << " ";
            }
            cerr << "]" << endl;
        #endif
        execv((char*)params.javaPath.c_str(), args);
    }


    void load_svd(execParams params) {
        // string jnalp = "-Djava.library.path=" + string(LIBRARIES_DIR);
        int count = 11;
        #ifdef DEVEL
            count = 12;
        #endif
        char *args[] = {
            (char*)"java",
            (char*)"-d32",
            (char*)"-client",
            (char*)"-Xmn1m",
            (char*)"-XX:NewRatio=1",
            (char*)"-Xms2m",
            (char*)"-Xmx32m",
            (char*)"-Dfile.encoding=UTF-8",
            // (char*)jnalp.c_str(),
            #ifndef DEVEL
                /* when not devel, use classes from assembly jar */
                (char*)"-jar",
                (char*)params.jar.c_str(),
            #else
                /* when devel, use classes from compile folders */
                (char*)"-cp",
                (char*)getClassPath(params.classPathFile).c_str(),
                (char*)params.mainClass.c_str(),
            #endif
            (char*)params.svdArg.c_str(),
            (char*)0
        };

        #ifdef DEVEL
            cerr << "Loading svd, with opts: [";
            for (int i = 0; i < count; i++) {
                cerr << args[i] << " ";
            }
            cerr << "]" << endl;
        #endif
        execv((char*)params.javaPath.c_str(), args);
    }
    

    void spawnBackgroundTask(execParams params, string lockFileName) {
        int i, lfp;
        char str[32];

        setsid(); /* obtain a new process group */
        #ifdef DEVEL
            cerr << "Booting ServeD" << endl;
        #endif

        string logFileName = params.svdArg + "-" + string(INTERNAL_LOG_FILE);
        freopen (logFileName.c_str(), "a+", stdout);
        freopen (logFileName.c_str(), "a+", stderr);
        umask(027); /* set newly created file permissions */

        bool userSpawn = false;
        if (params.svdArg == string(CORE_SVD_ID)) {
            cerr << "Spawning boot svd in dir: " << currentDir() << endl;
        } else {
            userSpawn = true;
            string homeDir = string(USERS_HOME_DIR) + params.svdArg;
            cerr << "Spawning user svd in home dir: " << homeDir << endl;
            chdir(homeDir.c_str());
        }

        lfp = open(lockFileName.c_str(), O_RDWR | O_CREAT, 0600);
        if (lfp < 0) {
            cerr << "Lock file occupied: " << lockFileName << ". Error: " << strerror(errno) << endl;
            exit(LOCK_FILE_OCCUPIED_ERROR); /* can not open */
        }

        if (lockf(lfp, F_TLOCK, 0) < 0) {
            cerr << "Cannot lock! Already spawned?" << endl;
            exit(CANNOT_LOCK_ERROR); /* can not lock */
        }

        /* first instance continues */
        sprintf(str, "%d\n", getpid());
        write(lfp, str, strlen(str)); /* record pid to lockfile */

        signal(SIGTSTP, SIG_IGN); /* ignore tty signals */
        signal(SIGTTOU, SIG_IGN);
        signal(SIGTTIN, SIG_IGN);
        signal(SIGHUP, defaultSignalHandler); /* catch hangup signal */
        signal(SIGTERM, defaultSignalHandler); /* catch kill signal */
        signal(SIGINT, defaultSignalHandler);

        /* spawn svd */
        #ifdef DEVEL
            chdir(coreDir.c_str()); /* change running directory before spawning svd in devel mode */
        #endif
            
        if (userSpawn) {
            params.jar = USER_JAR_FILE;
            load_svd(params);
        } else {
            params.jar = ROOT_JAR_FILE;
            load_svd64(params);
        }
    }
