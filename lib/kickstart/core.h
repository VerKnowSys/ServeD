/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/




#ifndef __CORE__
#define __CORE__


#include "../globals/globals.h"

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
#include <termios.h>
#include <signal.h>
#include <dirent.h>

#ifdef __FreeBSD__
    #include <kvm.h>
    #include <sys/capability.h>
    #include <libprocstat.h>
    #include <libutil.h>
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
    #include <util.h>
    #include <sys/ioctl.h>
#endif

#ifdef __linux__
    #include <stdlib.h>
    #include <signal.h>
    #include <sys/wait.h>
    #include <sys/fcntl.h>
    #include <algorithm>
    #include <pty.h>
    #define MAXPATHLEN PATH_MAX
#endif


    using namespace std;


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
