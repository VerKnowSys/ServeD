/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2012 - VerKnowSys
*/

#include "core.h"

int main(int argc, char const *argv[]) {

    int SVDWRITER = 0;
    int argument = 0;
    if (argc == 1) {
        cout << "No UID argument given for getProcessUsage(). Setting default to uid 0." << endl;
    } else {
        if (strcmp(argv[1], "GATHER") == 0) {
            if (argc >= 3)
                argument = atoi(argv[2]);
            else
                argument = 500;
            SVDWRITER = 1;
        } else {
            argument = atoi(argv[1]);
            SVDWRITER = 0;
        }
    }

    if (SVDWRITER == 0) {
        const int TIMES = 5;
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


        for (int z = 0; z < TIMES; z++) {
            int count;
            int COUNT_TIMES = 1000;
            cout << "Check NO: " << (z + 1) << endl;
            cout << processDataToLearn(argument) << endl;

            while (COUNT_TIMES > 0) {
                struct timespec tS;
                count = COUNT_TIMES;
                tS.tv_sec = 0;
                tS.tv_nsec = 0;
                clock_settime(CLOCK_REALTIME, &tS);
                while (count > 0) {
                    processDataToLearn(argument);
                    --count;
                }
                clock_gettime(CLOCK_REALTIME, &tS);
                cout << "Time taken for " << COUNT_TIMES << " rounds of processDataToLearn() is: " << tS.tv_sec << "s, " << tS.tv_nsec/1000 << "us (" << tS.tv_nsec/1000000 << "ms)" << endl;

                COUNT_TIMES /= 2;
            }
        }
    } else {
        // writer mode. Gather information from process for each second
        ofstream file;
        file.open("output_raw_processes.training", ios::app);
        int maxVal = 1200; // every second in 20 minutes
        for (int i = 0; i < maxVal; ++i) {
            cout << i + 1 << " of " << maxVal << endl;
            file << "PROC_BEGIN" << endl << processDataToLearn(argument) << "PROC_END" << endl << endl;
            sleep(1);
        }
        file.close();
    }

    return 0;
}
