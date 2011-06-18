/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include "core.h"


static string coreDir = currentDir();


using namespace std;


/* spawner core library functions */
extern "C" {
    

#ifdef DEVEL
    
    
    string getClassPath() {
        vector<string> modules;
        vector<string> libPaths;
        string postfixManaged = string(POSTFIX_MANAGED_JARS);
        string postfixTarget = string(POSTFIX_TARGET_CLASSES);
        modules.push_back(API_MODULE);
        modules.push_back(CORE_MODULE);
        modules.push_back(CLI_MODULE);
        modules.push_back(UTILS_MODULE);

        vector<string>::iterator it;
        /* list and create jar file list to pass to java */
        for (unsigned int ind = 0; ind < modules.size(); ind++) {
            string prefix = modules[ind] + postfixManaged;
            vector<string> files = vector<string>();
            getdir(prefix, files);
            for (unsigned int i = 0; i < files.size(); i++) {
                libPaths.push_back(prefix + files[i]);
            }
            libPaths.push_back(modules[ind] + postfixTarget);
        }
        
        /* also include scala */
        libPaths.push_back(currentDir() + string(SCALA_LIBRARY));
        
        /* include akka.conf to classpath */
        libPaths.push_back(currentDir() + string(AKKA_CONF));
        
        stringstream ret;
        for (it = libPaths.begin(); it < libPaths.end(); it++) {
            ret << *it << ":";
        }
        ret << ".";
        return ret.str();
    }
#endif


    void load_svd(string java_path, string jar, string mainClass, string svd_arg) {
        // string javalp = "-Djava.library.path=" + currentDir() + "/lib";
        string jnalp = "-Djna.library.path=" + currentDir() + "/lib";

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
            (char*)"-cp",
#ifndef DEVEL
            /* when not devel, use classes from assembly jar */
            (char*)jar.c_str(),
#else
            /* when devel, use classes from compile folders */
            (char*)getClassPath().c_str(),
#endif
            (char*)mainClass.c_str(),
            (char*)svd_arg.c_str(),
            (char*)0
        };

        stringstream    ret;
        ret << "loading svd, with opts: [";
        for (int i = 0; i < 12; i++) {
            ret << args[i] << " ";
        }
        ret << "]";
        log_message(ret.str());
        
        execv((char*)java_path.c_str(), args);
    }


    void spawnBackgroundTask(string abs_java_bin_path, string main_starting_class, string cmdline_param, bool bindSocket, string lockFileName) {
        int i, lfp;
        char str[32];

        if (getppid() == 1)
            return; /* already a daemon */
        
    	i = fork();
    	if (i < 0) {
            log_message("Fork error!");
            exit(1); /* fork error */
    	}

        if (i > 0) {
            /* blocking function for parent */
            if (bindSocket) {
                log_message("Spawning socket listener");
                createSocketServer();
            } else {
                return;
            }
        } 

    	/* child (daemon) continues */
    	setsid(); /* obtain a new process group */
        for (i = getdtablesize(); i >= 0; --i)
            close(i); /* close all descriptors */

        string logFileName = cmdline_param + "-" + string(LOG_FILE);
        freopen (logFileName.c_str(), "a+", stdout);
        freopen (logFileName.c_str(), "a+", stderr);
    	umask(027); /* set newly created file permissions */
    	
    	if (cmdline_param == string(CORE_SVD_ID)) {
    	    log_message("Spawning boot svd in dir: " + currentDir());
    	} else {
    	    string homeDir = string(USERS_HOME_DIR) + cmdline_param;
            log_message("Spawning user svd in home dir: " + homeDir);
            chdir(homeDir.c_str());
    	}

    	lfp = open(lockFileName.c_str(), O_RDWR | O_CREAT, 0640);
    	if (lfp < 0) {
            log_message("Cannot open!");
    	    exit(1); /* can not open */
    	}

    	if (lockf(lfp, F_TLOCK, 0) < 0) {
            log_message("Cannot lock! Already spawned?");
    	    exit(1); /* can not lock */
    	}

    	/* first instance continues */
    	sprintf(str, "%d\n", getpid());
    	write(lfp, str, strlen(str)); /* record pid to lockfile */
    	signal(SIGCHLD, SIG_IGN); /* ignore child */
    	signal(SIGTSTP, SIG_IGN); /* ignore tty signals */
    	signal(SIGTTOU, SIG_IGN);
    	signal(SIGTTIN, SIG_IGN);
    	signal(SIGHUP, defaultSignalHandler); /* catch hangup signal */
    	signal(SIGTERM, defaultSignalHandler); /* catch kill signal */
        signal(SIGINT, defaultSignalHandler);

    	/* spawn svd */
    	chdir(coreDir.c_str()); /* change running directory before spawning svd */
    	load_svd(abs_java_bin_path, (currentDir() + JAR_FILE), main_starting_class, cmdline_param);
    }


    char* spawn(char* _command) {
        ofstream        ofs;
        FILE            *fpipe = NULL;
        stringstream    ret, logFile;
        int             childExitStatus = 0;
        pid_t           pid = 0;
        pid_t           ppid = getppid();
        char            line[1024];
        uid_t           userUid;

        log_message("Spawning command: " + string(_command));
        if (!(fpipe = (FILE*)popen(_command, "r"))) {
            ret << POPEN_EXCEPTION;
            return (char*)(ret.str()).c_str();
        }
        pid = getpid();
        userUid = getuid();
        
        switch (userUid) {
            case 0:
                logFile << "/dev/null"; /* we're already handling log files for root */
                break;
            
            default:
                string cmdPrintable = escape(string(_command));
                logFile << string(USERS_HOME_DIR) << userUid << "/" << pid << "--" << cmdPrintable << ".log";
                break;
        }
        log_message("Logfile of spawn: " + logFile.str());
        
        ofs.open(logFile.str().c_str());
        while (fgets(line, sizeof(line), fpipe)) {
            ofs << line;
        }
        ofs.close();
        pclose(fpipe);

        ret << ppid << ";" << pid;
        return (char*)(ret.str()).c_str();
    }
    
    
    void createSocketServer() {
        int sockfd, newsockfd, servlen, n;
        socklen_t clilen;
        struct sockaddr_un  cli_addr, serv_addr;
        char buf[SOCK_DATA_PACKET_SIZE];

        if ((sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0)
            log_message("exception while creating socket");

        bzero((char *) &serv_addr, sizeof(serv_addr));
        serv_addr.sun_family = AF_UNIX;
        strcpy(serv_addr.sun_path, SOCK_FILE);
        servlen = strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_path) + sizeof(serv_addr.sun_family);

        if (bind(sockfd, (struct sockaddr *)&serv_addr, servlen) < 0) {
            log_message("exception while binding socket"); 
        }
        
        /* we also need to take care of socket server which spawns with own pid */
        pid_t sockPid = getpid();
        stringstream s;
        s << "Socket process pid: " << sockPid;
        log_message(s.str());
        umask(027); /* set newly created file permissions */
    	chdir(currentDir().c_str()); /* change running directory */
        char str[32];
    	int lfp = open(SOCKET_LOCK_FILE, O_RDWR | O_CREAT, 0640);
    	if (lfp < 0) {
            log_message("Cannot open!");
    	    exit(1); /* can not open */
    	}
    	if (lockf(lfp, F_TLOCK, 0) < 0) {
            log_message("Cannot lock! Already spawned?");
    	    exit(1); /* can not lock */
    	}
    	sprintf(str, "%d\n", sockPid);
        write(lfp, str, strlen(str)); /* record pid to lockfile */
        
        /* prepare listen on socket addr */
        listen(sockfd, 5);
        clilen = sizeof(cli_addr);
        
        /* main listening group */
        while (1) {
            newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
            if (newsockfd < 0)
                 log_message("exception on accepting socket");

            n = read(newsockfd, buf, SOCK_DATA_PACKET_SIZE);
            log_message("BUF: " + string(buf));
            close(newsockfd);
            
            /* spawn userland served */
            string cmd = currentDir() + "/userspawn " + string(buf);
            string result = spawn((char*)cmd.c_str());
            log_message("Result from spawn: " + result);
        }
        close(sockfd);
    }
    
    
    void sendSpawnMessage(char* content) {
        int sockfd, servlen,n;
        struct sockaddr_un  serv_addr;
        char buffer[SOCK_DATA_PACKET_SIZE];
        
        bzero((char *)&serv_addr, sizeof(serv_addr));
        serv_addr.sun_family = AF_UNIX;
        strncpy(serv_addr.sun_path, SOCK_FILE, strlen(SOCK_FILE) + 1);
        servlen = strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_path) + sizeof(serv_addr.sun_family);
        
        if ((sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0)
            log_message("Exception while creating socket");

        if (connect(sockfd, (struct sockaddr *)&serv_addr, servlen) < 0)
            log_message("Exception while connecting to socket server");

        bzero(buffer, SOCK_DATA_PACKET_SIZE);
        strncpy(buffer, content, strlen(content));
        write(sockfd, buffer, sizeof(buffer));
        close(sockfd);
    }

    
}

