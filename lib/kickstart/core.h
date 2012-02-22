/*
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#ifndef __CORE__
#define __CORE__


#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <iomanip>

#include <time.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/user.h>
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#ifndef __APPLE__
    #include <kvm.h>
    #include <sys/capability.h>
    #include <libprocstat.h>
#else
    #include <sys/fcntl.h>
#endif


    using namespace std;

    /* global constants */

    // #define DEVEL true
    #define APP_VERSION "0.1.7"
    #define COPYRIGHT "Copyright © 2oo9-2o12 VerKnowSys.com - All Rights Reserved."
    #define MOTD_FILE "/etc/motd"

    #ifdef __FreeBSD__
        #define DEFAULT_SHELL_COMMAND "/Software/Zsh/bin/zsh"
        #define DEFAULT_JAVA_BIN "/Software/Openjdk/bin/java"
        #define DEFAULT_JAVA64_BIN "/Software/Openjdk64/bin/java"
    #elif __APPLE__
        #define DEFAULT_SHELL_COMMAND "/usr/local/bin/zsh"
        #define DEFAULT_JAVA_BIN "/usr/bin/java"
        #define DEFAULT_JAVA64_BIN "/usr/bin/java"
    #else
        #error No supported OS found!
    #endif

    #define CORE_HOMEDIR "/SystemUsers/"
    #define USERS_HOME_DIR "/Users/"
    #define LIBRARIES_DIR "/lib/"

    #define CORE_SVD_ID "boot"
    #define SOCK_FILE "svd.sock"
    #define LOCK_FILE "svd-core.lock"
    #define SOCKET_LOCK_FILE "svd-ss.lock"
    #define INTERNAL_LOG_FILE "svd-diagnostics.log"
    #define ROOT_JAR_FILE "/sbin/root.core"
    #define USER_JAR_FILE "/bin/user.core"

    #define DEFAULT_USER_GROUP 0
    #define SOCK_DATA_PACKET_SIZE 32
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
        const char* getProcessUsage(int uid, bool consoleOutput = false);

    }

#endif
