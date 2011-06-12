/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
    
    Library to spawn jvm natively with given params.
    
*/

#include "svd_wrap.h"

#include <iostream>
#include <string>
#include <sys/stat.h> 
#include <stdio.h>
#include <fcntl.h>
#include <signal.h>
#include <unistd.h>


#define LOCK_FILE	"core-served.lock"
#define LOG_FILE	"core-served.log"
#define MAXPATHLEN  512


using namespace std;



string currentDir() {
   char temp[MAXPATHLEN];
   return (getcwd(temp, MAXPATHLEN) ? string( temp ) : string(""));
}


void load_svd(string java_path, string jar, string svd_arg, string mainClass) {
    
    // string javalp = "-Djava.library.path=" + currentDir() + "/lib";
    string jnalp = "-Djna.library.path=" + currentDir() + "/lib";
    
    char *args[] = {
        (char*)"java",
        (char*)"-XX:+UseCompressedOops",
        (char*)"-XX:MaxPermSize=512M",
        (char*)"-XX:+UseParallelGC",
        (char*)"-Xms64m",
        (char*)"-Xmx768m",
        (char*)"-Dfile.encoding=UTF-8",
        // (char*)javalp.c_str(),
        (char*)jnalp.c_str(),
        (char*)"-jar",
        (char*)jar.c_str(),
        (char*)mainClass.c_str(),
        (char*)svd_arg.c_str(),
        (char*)0
    };
    
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


void log_message(char *filename, char *message) {
    FILE *logfile;
	logfile = fopen(filename, "a+");
	if (!logfile) {
        cout << "Error opening log file!" << endl;
	    return;
	}
	fprintf(logfile, (char*)"%s\n", message);
	fclose(logfile);
}


void signal_handler(int sig) {
	switch(sig) {
	case SIGHUP:
		log_message((char*)LOG_FILE, (char*)"hangup signal catched");
		break;
	case SIGTERM:
		log_message((char*)LOG_FILE, (char*)"terminate signal catched");
		exit(0);
		break;
	}
}


void backgroundTask() {
    int i,lfp;
    char str[10];
    
	if(getppid()==1)
	    return; /* already a daemon */
	    
	i = fork();
	if (i < 0) {
        cout << "Fork error!" << endl;
        exit(1); /* fork error */
	}
	    
    if (i > 0) {
        cout << "Parent exits. Process spawned in background." << endl;
        exit(0); /* parent exits */
    } 
	
	/* child (daemon) continues */
	setsid(); /* obtain a new process group */
	for (i = getdtablesize(); i>=0; --i)
	    close(i); /* close all descriptors */
    i = open("/dev/null", O_RDWR);
    dup(i);
    dup(i); /* handle standart I/O */
	umask(027); /* set newly created file permissions */
	chdir(currentDir().c_str()); /* change running directory */
	lfp = open(LOCK_FILE, O_RDWR | O_CREAT, 0640);
	if (lfp < 0) {
        cout << "Cannot open!" << endl;
	    exit(1); /* can not open */
	}
	    
	if (lockf(lfp, F_TLOCK, 0) < 0) {
	    cout << "Cannot lock!" << endl;
	    exit(0); /* can not lock */
	}

    cout << "Looking for blue almonds.." << endl;	    
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
    string jarFile = currentDir() + "/svd.core/target/scala_2.9.0/core-assembly-1.0.jar";
    if (!fileExists(jarFile)) {
        cout << "No ServeD Core available. Rebuild svd.core first." << endl;
        exit(0);
    }
	
	load_svd("/usr/bin/java", jarFile, "com.verknowsys.served.boot", "core");
    
}


int main(int argc, char const *argv[]) {
    
    uid_t uid = getuid();
    
    if (uid != 0) {
        cout << "Respawn requires root privileges to run." << endl;
        exit(0);
    }
    
    
    cout << "Spawning in " << currentDir() << endl;
    backgroundTask();
    
    while(1) {
        sleep(1);
    } 
    
    return 0;
    
}
