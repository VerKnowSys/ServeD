#include "core.h"


int main(int argc, char const *argv[]) {

    static const int TIMES = 25;

    int argument = 0;
    int count;

    if (argc == 1) {
        cout << "No UID argument given for getProcessUsage(). Setting default to uid 0." << endl;
    } else {
        argument = atoi(argv[1]);
    }

    for (int z = 0; z < TIMES; z++) {

        int COUNT_TIMES = 1000;

        cout << "Show values check: " << z << endl;
        cout << getProcessUsage(argument, true) << endl;
        cout << getProcessUsage(argument) << endl;
        cout << getSocketUsage() << endl;

        while (COUNT_TIMES > 0) {
            timespec tS;

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


            count = COUNT_TIMES;
            tS.tv_sec = 0;
            tS.tv_nsec = 0;
            clock_settime(CLOCK_REALTIME, &tS);
            while (count > 0) {
                getSocketUsage();
                --count;
            }
            clock_gettime(CLOCK_REALTIME, &tS);
            cout << "Time taken for " << COUNT_TIMES << " rounds of getSocketUsage() is: " << tS.tv_sec << "s, " << tS.tv_nsec/1000 << "us (" << tS.tv_nsec/1000000 << "ms)" << endl;


            COUNT_TIMES /= 2;
        }
    }

    return 0;
}
