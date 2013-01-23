/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#ifndef __CORE__
#define __CORE__


#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <iomanip>
#include <paths.h>

#include <time.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/user.h>
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <limits.h>
#include <libgen.h>
#include <getopt.h>

#ifdef __FreeBSD__
    #include <kvm.h>
    #include <sys/capability.h>
    #include <libprocstat.h>
#endif

#ifdef __APPLE__
    #include <mach/clock.h>
    #include <mach/clock_priv.h>
    #include <mach/clock_types.h>
    #include <sys/fcntl.h>
    #include <assert.h>
    #include <errno.h>
    #include <stdbool.h>
    #include <stdlib.h>
    #include <stdio.h>
    #include <sys/sysctl.h>
#endif

#ifdef __linux__
    #include <stdlib.h>
    #include <signal.h>
    #include <sys/wait.h>
    #include <sys/fcntl.h>
    #include <algorithm>
    #define MAXPATHLEN PATH_MAX
#endif


    using namespace std;

    /* global constants */
    #define APP_VERSION "0.8.0"
    #define COPYRIGHT "Copyright © 2oo9-2o13 VerKnowSys.com - All Rights Reserved."
    #define MOTD_FILE "/etc/motd"

    /* default BSD case: */
    #define DEFAULT_SHELL_COMMAND "/Software/Zsh/exports/zsh"
    #define DEFAULT_JAVA_PATH "/Software/Openjdk/"
    #define DEFAULT_JAVA64_PATH "/Software/Openjdk-32/"
    #define DEFAULT_JAVA7_PATH "/Software/Openjdk7/"
    #define DEFAULT_JAVA764_PATH "/Software/Openjdk7-32/"

    #ifdef __FreeBSD__
        #ifndef JDK7
            #define DEFAULT_JAVA_BIN (DEFAULT_JAVA_PATH "exports/java")
            #define DEFAULT_JAVA64_BIN (DEFAULT_JAVA64_PATH "exports/java")
        #else
            #define DEFAULT_JAVA_BIN (DEFAULT_JAVA7_PATH "exports/java")
            #define DEFAULT_JAVA64_BIN (DEFAULT_JAVA764_PATH "exports/java")
        #endif
    #endif

    // Darwin case:
    #ifdef __APPLE__
        #define CLOCK_REALTIME REALTIME_CLOCK
        // NOTE: Darwin uses same zsh path as BSD
        #define DEFAULT_JAVA_BIN "/usr/bin/java"
        #define DEFAULT_JAVA64_BIN "/usr/bin/java"
    #endif

    // Linux case:
    #ifdef __linux__
        #undef DEFAULT_SHELL_COMMAND
        #define DEFAULT_SHELL_COMMAND "/bin/zsh"
        #define DEFAULT_JAVA_BIN (DEFAULT_JAVA_PATH "exports/java")
        #define DEFAULT_JAVA64_BIN "/usr/bin/java"
    #endif


    #define CORE_HOMEDIR "/SystemUsers/"
    #define SYSTEMUSERS_HOME_DIR "/SystemUsers" // no trailing slash
    #define USERS_HOME_DIR "/Users/"
    #define LIBRARIES_DIR "/lib/"
    #define LIBRARIES32_DIR "/lib32/"
    #define DEFAULT_BEHAVIORS_DIR "basesystem/behaviors"
    #define DEFAULT_BEHAVIORS_RAW "/output_raw_processes.raw.input"

    #define CORE_SVD_ID "boot"
    #define SOCK_FILE "svd.sock"
    #define LOCK_FILE "svd-core.lock"
    #define SOCKET_LOCK_FILE "svd-ss.lock"
    #define INTERNAL_LOG_FILE "svd-diagnostics.log"
    #define ROOT_JAR_FILE "/sbin/root.core"
    #define USER_JAR_FILE "/bin/user.core"

    #define DEFAULT_USER_UID 501
    #define DEFAULT_GATHERING_PAUSE_MICROSECONDS 500000 // half a second
    #define DEFAULT_COUNT_OF_ROUNDS_OF_GATHERING 7200 // waiting half a second, hence 7200 is 60 minutes of gathering
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
        int isSymlink(const char *path);
        const char* getProcessUsage(int uid, bool consoleOutput = false);
        const char* processDataToLearn(int uid);

    }

#endif
