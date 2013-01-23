/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
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
        string javaNative = DEFAULT_JAVA64_PATH;
        #ifdef JDK7
            javaNative = DEFAULT_JAVA764_PATH;
        #endif

        string libPath = "LD_LIBRARY_PATH=" + javaNative + "lib:" + javaNative + "openjdk6/jre/lib/amd64:" + javaNative + "openjdk6/jre/lib/amd64/client" + ":/lib";
        const char *args[] = {
            // libPath.c_str(),
            DEFAULT_JAVA64_BIN,
            "-d64",
            "-Xmn4m",
            "-Xms16m",
            "-Xmx256m",
            "-XX:+UseCompressedOops",
            "-Dfile.encoding=UTF-8",
            "-Djava.awt.headless=true",
            jnalp.c_str(),
            #ifndef DEVEL
                /* when not devel, use classes from assembly jar */
                "-jar",
                params.jar.c_str(),
            #else
                "-javaagent:/lib/jrebel/jrebel.jar", // XXX: hardcoded
                // "-Dcom.sun.management.jmxremote=false",
                // "-Dcom.sun.management.jmxremote.ssl=false",
                // "-Dcom.sun.management.jmxremote.authenticate=false", // XXX: TODO: Security hole
                // "-Dcom.sun.management.jmxremote.port=55555",
                // /* when devel, use classes from compile folders */
                "-cp",
                getClassPath(params.classPathFile).c_str(),
                params.mainClass.c_str(),
            #endif
            params.svdArg.c_str(),
            NULL
        };

        #ifdef DEVEL
            cerr << "Loading svd-64, with opts: [";
            for (int i = 0; args[i] != NULL; i++) {
                cerr << args[i] << " ";
            }
            cerr << "]" << endl;
        #endif
        execv((char *) params.javaPath.c_str(), (char **) args);
    }


    void load_svd(execParams params) {
        string jnalp = "-Djava.library.path=" + string(LIBRARIES32_DIR);
        string javaNative = DEFAULT_JAVA_PATH;
        #ifdef JDK7
            javaNative = DEFAULT_JAVA7_PATH;
        #endif

        string libPath = "LD_LIBRARY_PATH=" + javaNative + "lib:" + javaNative + "openjdk6/jre/lib/i386:" + javaNative + "openjdk6/jre/lib/i386/client" + ":/lib32";
        const char *args[] = {
            // libPath.c_str(),
            DEFAULT_JAVA_BIN,
            "-d32",
            "-client",
            "-Xmn1m",
            "-XX:NewRatio=1",
            "-Xms32m",
            "-Xmx64m",
            "-Dfile.encoding=UTF-8",
            "-Djava.awt.headless=true",
            jnalp.c_str(),
            #ifndef DEVEL
                /* when not devel, use classes from assembly jar */
                "-jar",
                params.jar.c_str(),
            #else
                "-javaagent:/lib/jrebel/jrebel.jar", // XXX
                /* when devel, use classes from compile folders */
                "-cp",
                getClassPath(params.classPathFile).c_str(),
                params.mainClass.c_str(),
            #endif
            params.svdArg.c_str(),
            NULL
        };

        #ifdef DEVEL
            cerr << "Loading svd, with opts: [";
            for (int i = 0; args[i] != NULL; i++) {
                cerr << args[i] << " ";
            }
            cerr << "]" << endl;
        #endif
        execv((char *) params.javaPath.c_str(), (char **) args);
    }


    void spawnBackgroundTask(execParams params, string lockFileName) {
        int lfp;
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
            #ifndef DEVEL
                params.jar = USER_JAR_FILE;
            #endif
            load_svd(params);
        } else {
            #ifndef DEVEL
                params.jar = ROOT_JAR_FILE;
            #endif
            load_svd64(params);
        }
    }
