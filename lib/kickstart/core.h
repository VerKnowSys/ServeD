/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <vector>
#include <sys/stat.h> 
#include <fcntl.h>
#include <signal.h>


using namespace std;


/* global constants */
#ifndef __CORE__
#define __CORE__

    #define DEVEL
    #define SVD_PARAM_ROOT "svd"
    #define SVD_PARAM_USER "user"
    #define DEFAULT_SHELL_COMMAND "/usr/local/bin/zsh"
    #define CORE_CLASSPATH_FILE "/tmp/core.classpath"
    #define DEFAULT_JAVA_BIN "/usr/bin/java"
    #define CORE_HOMEDIR "/SystemUsers/Core"
    #define USERS_HOME_DIR "/Users/"
    #define LOCK_FILE	"svd-core.lock"
    #define SOCKET_LOCK_FILE "svd-ss.lock"
    #define JAR_FILE    "/svd.core/target/scala_2.9.0/core_2.9.0-1.0-onejar.jar"
    #define INTERNAL_LOG_FILE "svd-diagnostics.log"
    #define SOCK_FILE   "svd.sock"
    #define CORE_SVD_ID "boot"
    #define DEFAULT_USER_GROUP 0
    #define SOCK_DATA_PACKET_SIZE 32
    #define MAXPATHLEN  512

    #define LOCK_FILE_OCCUPIED_ERROR 100
    #define CANNOT_LOCK_ERROR 101
    #define POPEN_ERROR 102
    #define CLASSPATH_DIR_MISSING_ERROR 103
    #define NOROOT_PRIVLEGES_ERROR 104
    #define SETUID_ERROR 105
    #define SETGID_ERROR 106
    #define FORK_ERROR 107
    #define EXEC_ERROR 108
    #define NO_UID_GIVEN_ERROR 109
    #define DIAGNOSTIC_LOG_ERROR 110
    #define AMBIGOUS_ENTRY_ERROR 111

    #ifdef DEVEL

        #define MAIN_CLASS "com.verknowsys.served.boot"

        string getClassPath();
    
    #endif

#endif
