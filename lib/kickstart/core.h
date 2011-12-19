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

    #define DEVEL true
    #define APP_VERSION "0.1.2"
    #define COPYRIGHT "Copyright © 2oo9-2o11 VerKnowSys.com - All Rights Reserved."
    #define MOTD_FILE "/etc/motd"

    #ifdef __FreeBSD__
        #define DEFAULT_SHELL_COMMAND "/Software/Zsh-4.3.10/bin/zsh"
        #define DEFAULT_JAVA_BIN "/Software/Openjdk7/bin/java"
    #elif __linux__ 
        #define DEFAULT_SHELL_COMMAND "/Software/Zsh-4.3.10/bin/zsh"
        #define DEFAULT_JAVA_BIN "/usr/bin/java"
    #elif __APPLE__
        #define DEFAULT_SHELL_COMMAND "/usr/local/bin/zsh"
        #define DEFAULT_JAVA_BIN "/usr/bin/java"
    #else
        #error No supported OS found!
    #endif

    #define CORE_HOMEDIR "/SystemUsers/"
    #define USERS_HOME_DIR "/Users/"
    #define LIBRARIES_DIR "/lib/"

    #define CORE_SVD_ID "boot"
    #define SOCK_FILE "svd.sock"
    #define LOCK_FILE   "svd-core.lock"
    #define SOCKET_LOCK_FILE "svd-ss.lock"
    #define INTERNAL_LOG_FILE "svd-diagnostics.log"
    #define ROOT_JAR_FILE "/sbin/root.core"
    #define USER_JAR_FILE "/bin/user.core"

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
    #define ROOT_UID_ERROR 112
    #define INSTALLATION_MISSING_ERROR 113

    struct execParams {
        string javaPath;
        #ifdef DEVEL
            string classPathFile;
            string mainClass;
        #else
            string jar;
        #endif
        string svdArg;
    };

    #ifdef DEVEL

        #define ROOT_MAIN_CLASS "com.verknowsys.served.rootboot"
        #define USER_MAIN_CLASS "com.verknowsys.served.userboot"
        #define ROOT_CLASSPATH_FILE "/tmp/root.classpath"
        #define USER_CLASSPATH_FILE "/tmp/user.classpath"

        string getClassPath(string classPathFile);

    #endif

    extern "C" {

        int getOwner(char* path);

    }

#endif
