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
#define JAR_FILE    "/svd.core/target/scala_2.9.0/core_2.9.0-1.0-onejar.jar"
#define LOG_FILE    "svd.log"
#define INTERNAL_LOG_FILE "svd.diagnostics.log"
#define SOCK_FILE   "svd.sock"
#define SOCK_DATA_PACKET_SIZE 128
#define CORE_SVD_ID "boot"


#ifdef DEVEL

/* svd modules definitions */

#define MAIN_CLASS "com.verknowsys.served.boot"
#define API_MODULE "svd.api"
#define CORE_MODULE "svd.core"
#define UTILS_MODULE "svd.utils"

#define POSTFIX_MANAGED_JARS "/lib_managed/scala_2.9.0/compile/"
#define POSTFIX_TARGET_CLASSES "/target/scala_2.9.0/classes/"
#define SCALA_LIBRARY "/project/boot/scala-2.9.0/lib/scala-library.jar"
#define AKKA_CONF "/svd.core/src/main/resources/"

#endif


using namespace std;

extern bool fileExists(string strFilename);
extern string currentDir();
extern bool processAlive(pid_t pid);
extern void log_message(string message);
extern void defaultSignalHandler(int sig);
extern string escape(string input);
extern int getdir (string dir, vector<string> &files);
extern void cleanupLockAndSockFIles();

extern "C" {
    
    /* function prototypes */
    char*   spawn(char* _command);
    void    spawnBackgroundTask(string abs_java_bin_path, string main_starting_class, string cmdline_param, bool bindSocket, string lockFileName);
    
    void    createSocketServer();
    void    sendSpawnMessage(char* content);

#ifdef DEVEL

    string getClassPath();
    
#endif

}
