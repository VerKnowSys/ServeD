/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include "core.h"


const static string coreDir = currentDir();


using namespace std;


/* spawner core library functions */
extern "C" {
    

#ifdef DEVEL
    
    string getClassPath() {
        string strink = coreDir + "/tmp/core.classpath";
        string cp = "";
        ifstream f(strink.c_str());
        if (f.is_open()){
            f >> cp;
            f.close();
        } else {
            cerr << strink << " not found!" << endl;
            exit(-1);
        }
        return cp;
    }
    
#endif


    void load_svd(string java_path, string jar, string mainClassParam, string svd_arg) {
        // string javalp = "-Djava.library.path=" + currentDir() + "/lib";
        string jnalp = "-Djna.library.path=" + currentDir() + "/lib";

#ifdef DEVEL
    #define COUNT 14
#else
    #define COUNT 13
#endif
        char *args[] = {
            (char*)"java",
            (char*)"-d64",
            (char*)"-XX:+UseCompressedOops",
            (char*)"-XX:MaxPermSize=256M",
            (char*)"-XX:+UseParallelGC",
            (char*)"-Xms64m",
            (char*)"-Xmx512m",
            (char*)"-Dfile.encoding=UTF-8",
            // (char*)javalp.c_str(),
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

        cerr << "Loading svd, with opts: [";
        for (int i = 0; i < COUNT; i++) {
            cerr << args[i] << " ";
        }
        cerr << "]" << endl;
        execv((char*)java_path.c_str(), args);
    }


    void spawnBackgroundTask(string abs_java_bin_path, string main_starting_class_param, string cmdline_param, string lockFileName) {
        int i, lfp;
        char str[32];

        // if (getppid() == 1)
        //     return; /* already a daemon */
        
        // i = fork();
        // if (i < 0) {
        //             log_message("Fork error!");
        //             exit(1); /* fork error */
        // }

    	/* child (daemon) continues */
    	setsid(); /* obtain a new process group */
        cerr << "Sid set" << endl;
        // for (i = getdtablesize(); i >= 0; --i)
        //             close(i); /* close all descriptors */

        // string logFileName = cmdline_param + "-" + string(LOG_FILE);
        //         freopen (logFileName.c_str(), "a+", stdout);
        //         freopen (logFileName.c_str(), "a+", stderr);
    	umask(027); /* set newly created file permissions */
    	
    	if (cmdline_param == string(CORE_SVD_ID)) {
            cerr << "Spawning boot svd in dir: " << currentDir() << endl;
    	} else {
    	    string homeDir = string(USERS_HOME_DIR) + cmdline_param;
            cerr << "Spawning user svd in home dir: " << homeDir << endl;
            chdir(homeDir.c_str());
    	}

    	lfp = open(lockFileName.c_str(), O_RDWR | O_CREAT, 0640);
    	if (lfp < 0) {
            cerr << "Cannot open!" << endl;
    	    exit(1); /* can not open */
    	}

        // if (lockf(lfp, F_TLOCK, 0) < 0) {
        //             cerr << "Cannot lock! Already spawned?" << endl;
        //     exit(1); /* can not lock */
        // }

    	/* first instance continues */
    	sprintf(str, "%d\n", getpid());
    	write(lfp, str, strlen(str)); /* record pid to lockfile */
    	
    	// signal(SIGCHLD, SIG_IGN); /* ignore child */
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


    void spawn(string uid) {
        ofstream        ofs;
        FILE            *fpipe = NULL;
        char            line[SOCK_DATA_PACKET_SIZE];
        int             childExitStatus = 0;
        pid_t           pid = getpid();
        pid_t           ppid = getppid();
        uid_t           userUid = getuid();

        cerr << "Spawning command: " << uid << endl;
        if (!(fpipe = (FILE*)popen(uid.c_str(), "r"))) {
            cerr << POPEN_EXCEPTION << endl;
            return;
        }

        #ifdef DEVEL
            cerr << "Spawn pid: " << pid << endl;
            cerr << "Spawn ppid: " << ppid << endl;
            cerr << "Spawn uid: " << userUid << endl;
        #endif
        
        switch (userUid) {
            case 0:
                break;
            
            default:
                string cmdPrintable = escape(uid);
                stringstream s;
                s << string(USERS_HOME_DIR) << userUid << "/" << pid << "--" << cmdPrintable << ".log";
                ofs.open(s.str().c_str());
                while (fgets(line, sizeof(line) + sizeof((char*)0), fpipe)) {
                    #ifdef DEVEL
                        cerr << "Output: " << line << endl;
                    #endif
                    ofs << line;
                }
                ofs.close();
                pclose(fpipe);
                break;
        }
    }
    
    
}

