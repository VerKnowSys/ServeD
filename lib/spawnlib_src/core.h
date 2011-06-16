/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include <iostream>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <limits.h>
#include <sys/stat.h> 
// #include <stdio.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/socket.h>
#include <sys/un.h>


/* global constants */

#define USERS_HOME_DIR "/Users/"
#define MAXPATHLEN  512
#define CHILD_EXCEPTION 250
#define SETUID_EXCEPTION 251
#define POPEN_EXCEPTION 252
#define FORK_EXCEPTION 253
#define EXIT_FAILURE_EXCEPTION 254
#define LOCK_FILE	"svd-core.lock"
#define SOCKET_LOCK_FILE "svd-ss.lock"
#define JAR_FILE    "/svd.core/target/scala_2.9.0/core-assembly-1.0.jar"
#define LOG_FILE    "svd.log"
#define INTERNAL_LOG_FILE "svd.diagnostic.log"
#define SOCK_FILE   "svd.sock"
#define SOCK_DATA_PACKET_SIZE 128
#define CORE_SVD_ID "boot"


using namespace std;


extern "C" {
    
    /* function prototypes */
    char*   spawn(char* _command);
    void    spawnBackgroundTask(string abs_java_bin_path, string main_starting_class, string cmdline_param, bool bindSocket, string lockFileName);

    string  currentDir();
    bool    fileExists(string strFilename);
    bool    processAlive(pid_t pid);
    
    void    createSocketServer();
    void    sendSpawnMessage(char* content);
    void    performCleanup();

    void    log_message(string message);

}
