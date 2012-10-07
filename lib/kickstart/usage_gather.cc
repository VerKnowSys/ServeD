/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2012 - VerKnowSys
*/

#include "core.h"


extern vector<string> split(const string& s, const string& delim, const bool keep_empty = true);


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
                #ifndef __APPLE__
                clock_settime(CLOCK_REALTIME, &tS);
                #endif
                while (count > 0) {
                    getProcessUsage(argument);
                    --count;
                }
                #ifndef __APPLE__
                clock_gettime(CLOCK_REALTIME, &tS);
                #endif
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
                #ifndef __APPLE__
                clock_settime(CLOCK_REALTIME, &tS);
                #endif
                while (count > 0) {
                    processDataToLearn(argument);
                    --count;
                }
                #ifndef __APPLE__
                clock_gettime(CLOCK_REALTIME, &tS);
                #endif
                cout << "Time taken for " << COUNT_TIMES << " rounds of processDataToLearn() is: " << tS.tv_sec << "s, " << tS.tv_nsec/1000 << "us (" << tS.tv_nsec/1000000 << "ms)" << endl;

                COUNT_TIMES /= 2;
            }
        }
    } else {
        // writer mode. Gather information from process for each second
        ofstream file, file_per_process;
        file.open("output_raw_processes.before-train", ios::app);
        // file2.open("output_raw_processes.training", ios::app);
        int maxVal = 1200, oldPid = 0, pid = 0; // every second in 20 minutes
        for (int i = 0; i < maxVal; ++i) {
            cout << "Iteration " << i + 1 << " of " << maxVal << endl;
            const string data = processDataToLearn(argument);
            const vector<string> values = split(data, "\n");
            for (int it = 0; it < values.size(); it++) {
                const vector<string> two_sides = split(values.at(it), "#");
                const vector<string> process_name_and_pid = split(two_sides.front(), " ");
                const string procName = process_name_and_pid.front();
                const string procPid = process_name_and_pid.back();
                if (procName != "") {
                    stringstream(procPid) >> pid;

                    mkdir("basesystem/behaviors", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
                    string fileName = "basesystem/behaviors/" + procName + ".input";

                    file_per_process.open(fileName.c_str(), ios::app);
                    file_per_process << two_sides.back(); // write list of pid states
                    if (oldPid == pid)
                        file_per_process << endl << endl;
                    else
                        file_per_process << endl;
                    file_per_process.close();
                    // cout << "name: " << procName << ", pid: " << procPid << ", file: " << fileName << endl;

                    stringstream(procPid) >> oldPid; // store current pid number
                }
            }

            file << "PROC_BEGIN" << endl << data << "PROC_END" << endl << endl;
            sleep(1);
        }
        file.close();
        // file2.close();
    }

    return 0;
}
