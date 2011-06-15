/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/

#include "core.h"


using namespace std;


/* spawner core library functions */
extern "C" {
    

    string currentDir() {
       char temp[MAXPATHLEN];
       return (getcwd(temp, MAXPATHLEN) ? string( temp ) : string(""));
    }


    void log_message(string message) {
        FILE *logfile;
    	logfile = fopen(INTERNAL_LOG_FILE, "a+");
    	if (!logfile) {
    	    return;
    	}
    	fprintf(logfile, (char*)"%s\n", message.c_str());
    	fclose(logfile);
    }


    void load_svd(string java_path, string jar, string mainClass, string svd_arg) {
        // string javalp = "-Djava.library.path=" + currentDir() + "/lib";
        string jnalp = "-Djna.library.path=" + currentDir() + "/lib";

        char *args[] = {
            (char*)"java",
            (char*)"-XX:+UseCompressedOops",
            (char*)"-XX:MaxPermSize=256M",
            (char*)"-XX:+UseParallelGC",
            (char*)"-Xms64m",
            (char*)"-Xmx512m",
            (char*)"-Dfile.encoding=UTF-8",
            // (char*)javalp.c_str(),
            (char*)jnalp.c_str(),
            (char*)"-cp",
            (char*)jar.c_str(),
            (char*)mainClass.c_str(),
            (char*)svd_arg.c_str(),
            (char*)0
        };

        stringstream    ret;
        ret << "loading svd, with opts: [";
        for (int i = 0; i < 13; i++) {
            ret << args[i] << " ";
        }
        ret << "]";
        log_message(ret.str());

        execv((char*)java_path.c_str(), args);

    }


    bool fileExists(string strFilename) { 
        struct stat stFileInfo; 
        bool blnReturn; 
        int intStat; 

        intStat = stat(strFilename.c_str(),&stFileInfo); 
        if(intStat == 0) { 
            blnReturn = true; 
        } else { 
            blnReturn = false; 
        } 

        return blnReturn; 
    }


    void signal_handler(int sig) {
    	switch(sig) {
    	case SIGHUP:
    		log_message("SIGHUP (hangup) signal catched");
    		break;
    	case SIGTERM:
    		log_message("SIGTERM (terminate) signal catched");
    		exit(0);
    		break;
    	}
    }


    void spawnBackgroundTask(string abs_java_bin_path, string main_starting_class, string cmdline_param, bool bindSocket) {
        int i,lfp;
        char str[32];

    	if(getppid()==1)
    	    return; /* already a daemon */

    	i = fork();
    	if (i < 0) {
            log_message("Fork error!");
            exit(1); /* fork error */
    	}

        if (i > 0) {
            log_message("Parent exits.");
            
            /* blocking function for parent */
            if (bindSocket) {
                log_message("Spawning socket listener");
                create_socket_server();
            }
        } 

    	/* child (daemon) continues */
    	setsid(); /* obtain a new process group */
        for (i = getdtablesize(); i >= 0; --i)
            close(i); /* close all descriptors */

        // i = open("/dev/null", O_RDWR);
        // dup(i);
        // dup(i); /* handle standart I/O */
        freopen ("/dev/null", "a+", stdout);
        freopen (LOG_FILE, "a+", stderr);
    	umask(027); /* set newly created file permissions */
    	chdir(currentDir().c_str()); /* change running directory */

    	lfp = open(LOCK_FILE, O_RDWR | O_CREAT, 0640);
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
    	signal(SIGHUP, signal_handler); /* catch hangup signal */
    	signal(SIGTERM, signal_handler); /* catch kill signal */

    	/* spawn svd */
    	load_svd(abs_java_bin_path, (currentDir() + JAR_FILE), main_starting_class, cmdline_param);
    }


    char* spawn(char* _command) {
        FILE            *fpipe = NULL;
        stringstream    ret;
        int             childExitStatus = 0;
        pid_t           pid = 0;
        pid_t           ppid = getppid();
        char            line[256];

        log_message("Spawning command: " + string(_command));
        if (!(fpipe = (FILE*)popen(_command, "r"))) {
            ret << POPEN_EXCEPTION;
            return (char*)(ret.str()).c_str();
        }
        while (fgets(line, sizeof(line), fpipe)) {
            ret << string(line);
        }

        // fpipe = NULL;
        // if (childExitStatus == -1) {
        //     ret << CHILD_EXCEPTION;
        //     return (char*)(ret.str()).c_str();
        // }

        // setbuf(stdout, NULL);
        // setbuf(stderr, NULL);
        pid = getpid();
        pclose(fpipe);
        ret << ppid << ";" << pid;
        return (char*)(ret.str()).c_str();
    }
    
    
    void create_socket_server() {
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

        /* cleaning old socket files */
        string rmSocket = ("/bin/rm " + string(SOCK_FILE)); // 2011-06-15 03:34:40 - dmilith - XXX: might be hacky but works?
        system(rmSocket.c_str());

        if (bind(sockfd, (struct sockaddr *)&serv_addr, servlen) < 0) {
            log_message("exception while binding socket"); 
        }

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
            
            log_message("TODO: Spawning userland ServeD");
            // spawnBackgroundTask("/usr/bin/java", "com.verknowsys.served.userboot", "dmilith", false);
            
        }

        close(sockfd);
    }
    
    
    void send_socket_message(char* content) {
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

