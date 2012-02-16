
#include "core.h"

#include <sys/cdefs.h>
#include <kvm.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <paths.h>
#include <fcntl.h>
#include <signal.h>
#include <unistd.h>
#include <ctype.h>
#include <err.h>
#include <netdb.h>
#include <pwd.h>
#include <stdarg.h>
#include <sys/sysctl.h>
#include <sys/user.h>
#include <sys/proc.h>
#include <sys/un.h>
#include <time.h>
#include <sys/stat.h>
#include <libutil.h>


int main(int argc, char const *argv[]) {

    const int TIMES = 5;
    int argument = 0;
    if (argc == 1) {
        cout << "No UID argument given for getProcessUsage(). Setting default to uid 0." << endl;
    } else
        argument = atoi(argv[1]);

    for (int z = 0; z < TIMES; z++) {
        int count;
        int COUNT_TIMES = 1000;
        cout << "Check NO: " << (z + 1) << endl;
        cout << getProcessUsage(argument) << endl;

        while (COUNT_TIMES > 0) {
            struct timespec tS;
            count = COUNT_TIMES;
            tS.tv_sec = 0;
            tS.tv_nsec = 0;
            clock_settime(CLOCK_REALTIME, &tS);
            while (count > 0) {
                getProcessUsage(argument);
                --count;
            }
            clock_gettime(CLOCK_REALTIME, &tS);
            cout << "Time taken for " << COUNT_TIMES << " rounds of getProcessUsage() is: " << tS.tv_sec << "s, " << tS.tv_nsec/1000 << "us (" << tS.tv_nsec/1000000 << "ms)" << endl;

            COUNT_TIMES /= 2;
        }
    }

    return 0;
}
