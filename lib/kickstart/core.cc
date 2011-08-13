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
    
        string getClassPath() {
            string core = coreDir + CORE_CLASSPATH_FILE;
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


    void load_svd(string java_path, string jar, string mainClassParam, string svd_arg) {
        string jnalp = "-Djava.library.path=" + currentDir() + "/lib";
        #ifdef DEVEL
            #define COUNT 14
        #else
            #define COUNT 13
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
            (char*)jnalp.c_str(),
            #ifndef DEVEL
                /* when not devel, use classes from assembly jar */
                (char*)"-jar",
                (char*)jar.c_str(),
            #else
                /* when devel, use classes from compile folders */
                (char*)"-cp",
                (char*)getClassPath().c_str(),
                (char*)MAIN_CLASS,
            #endif
            (char*)mainClassParam.c_str(),
            (char*)svd_arg.c_str(),
            (char*)0
        };

        #ifdef DEVEL
            cerr << "Loading svd, with opts: [";
            for (int i = 0; i < COUNT; i++) {
                cerr << args[i] << " ";
            }
            cerr << "]" << endl;
        #endif
        execv((char*)java_path.c_str(), args);
    }


    void spawnBackgroundTask(
        string abs_java_bin_path,
        string main_starting_class_param,
        string cmdline_param,
        string lockFileName) {

        int i, lfp;
        char str[32];

    	setsid(); /* obtain a new process group */
    	#ifdef DEVEL
            cerr << "Booting ServeD" << endl;
        #endif

        string logFileName = cmdline_param + "-" + string(INTERNAL_LOG_FILE);
        freopen (logFileName.c_str(), "a+", stdout);
        freopen (logFileName.c_str(), "a+", stderr);
    	umask(027); /* set newly created file permissions */
    	
    	if (cmdline_param == string(CORE_SVD_ID)) {
            cerr << "Spawning boot svd in dir: " << currentDir() << endl;
    	} else {
    	    string homeDir = string(USERS_HOME_DIR) + cmdline_param;
            cerr << "Spawning user svd in home dir: " << homeDir << endl;
            chdir(homeDir.c_str());
    	}

    	lfp = open(lockFileName.c_str(), O_RDWR | O_CREAT, 0600);
    	if (lfp < 0) {
            cerr << "Lock file occupied: " << lockFileName << ". Cannot open." << endl;
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
    	chdir(coreDir.c_str()); /* change running directory before spawning svd */
    	load_svd(abs_java_bin_path, (coreDir + JAR_FILE), main_starting_class_param, cmdline_param);
    }
