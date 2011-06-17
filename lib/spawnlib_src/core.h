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
#include <vector>
#include <sys/wait.h>
#include <limits.h>
#include <sys/stat.h> 
// #include <stdio.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <dirent.h>
#include <errno.h>

/* global constants */

#define DEVEL true
#define USERS_HOME_DIR "/Users/"
#define MAXPATHLEN  512
#define SETUID_EXCEPTION 251
#define POPEN_EXCEPTION 252
#define FORK_EXCEPTION 253
#define EXIT_FAILURE_EXCEPTION 254
#define LOCK_FILE	"svd-core.lock"
#define SOCKET_LOCK_FILE "svd-ss.lock"
#define JAR_FILE    "/svd.core/target/scala_2.9.0/core-assembly-1.0.jar"
#define LOG_FILE    "svd.log"
#define INTERNAL_LOG_FILE "svd.diagnostics.log"
#define SOCK_FILE   "svd.sock"
#define SOCK_DATA_PACKET_SIZE 128
#define CORE_SVD_ID "boot"


#ifdef DEVEL

/* svd modules definitions */
#define API_MODULE "svd.api"
#define CORE_MODULE "svd.core"
#define CLI_MODULE "svd.cli"
#define UTILS_MODULE "svd.utils"

#define POSTFIX_MANAGED_JARS "/lib_managed/scala_2.9.0/compile/"
#define POSTFIX_TARGET_CLASSES "/target/scala_2.9.0/classes/"
#define SCALA_LIBRARY "/project/boot/scala-2.9.0/lib/scala-library.jar"

#endif


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
    
#ifdef DEVEL

    string getClassPath();
    
#endif

}
