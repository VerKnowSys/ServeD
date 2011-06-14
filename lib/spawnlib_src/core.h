/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <limits.h>
#include <sys/stat.h> 
#include <stdio.h>
#include <fcntl.h>
#include <signal.h>


/* global constants */

#define MAXPATHLEN  512
#define CHILD_EXCEPTION 250
#define SETUID_EXCEPTION 251
#define POPEN_EXCEPTION 252
#define FORK_EXCEPTION 253
#define EXIT_FAILURE_EXCEPTION 254
#define LOCK_FILE	"/var/run/svd-core.lock"
#define JAR_FILE    "/svd.core/target/scala_2.9.0/core-assembly-1.0.jar"
#define LOG_FILE    "/var/log/svd.log"
#define INTERNAL_LOG_FILE "/var/log/svd.diagnostic.log"


using namespace std;


extern "C" {
    
    /* function prototypes */
    char*   spawn(char* _command);
    void    log_message(string message);
    bool    fileExists(string strFilename);
    void    spawnBackgroundTask(string abs_java_bin_path, string main_starting_class, string cmdline_param);
    string  currentDir();

}
