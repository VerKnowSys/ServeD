/*
    Author: Daniel (dmilith) Dettlaff
    © 2012 - VerKnowSys
*/

#include "core.h"


#include <kvm.h>
#include <sys/sysctl.h>
#include <sys/user.h>
#include <sys/proc.h>
#include <paths.h>

#define ord(c) ((int)(unsigned char)(c))


extern "C" {



    const char* getProcessUsage(int uid, bool consoleOutput = false) {

        int count = 0;
        char** args = NULL;
        string command, output;

        kvm_t* kd = kvm_open(NULL, "/dev/mem", NULL, O_RDONLY, "kvm_open");
        if (kd == 0) {
            if (consoleOutput)
                cerr << "Error initializing kernel descriptor!" << endl;
            return (char*)"KDERR";
        }

        kinfo_proc* procs;
        // for (int yy = 0; yy < 1000; yy++)
            procs = kvm_getprocs(kd, KERN_PROC_UID, uid, &count); // get processes directly from BSD kernel

        if (count <= 0) {
            if (consoleOutput)
                cerr << "No processes for given UID!" << endl;
            return (char*)"NOPCS";
        }

        if (consoleOutput) {
            cout << "Process count: " << count << ". Owner UID: " << uid << endl;
            cout << setiosflags(ios::left)
                << setw(6) << "| NO:"
                << setw(27) << "| NAME:"
                << setw(52) << "| CMD:"
                << setw(10) << "| PID:"
                << setw(10) << "| PPID:"
                << setw(10) << "| RSS:"
                << setw(10) << "| MRSS:"
                << setw(16) << "| RUN-TIME(ms):"
                << setw(12) << "| BLK-IN:"
                << setw(12) << "| BLK-OUT:"
                << setw(6) << "| THR:"
                << setw(6) << "| PRI-NRML:"
                << endl;
        }

        for (int i = 0; i < count; ++i) {
            stringstream out;
            command = "";
            args = kvm_getargv(kd, procs, 0);

            for (int y = 0; (args != 0) && (args[y] != 0); y++)
                if (y == 0)
                    command = string(args[y]);
                else
                    command += " " + string(args[y]);

            out << setiosflags(ios::left)
                << "| " << setw(4) << (i + 1)
                << "| " << setw(25) << (procs->ki_comm)
                << "| " << setw(50) << (command)
                << "| " << setw(8) << (procs->ki_pid)
                << "| " << setw(8) << (procs->ki_ppid)
                << "| " << setw(8) << (procs->ki_rssize * 4)
                << "| " << setw(8) << (procs->ki_rusage.ru_maxrss * 4)
                << "| " << setw(14) << (procs->ki_runtime / 1000)
                << "| " << setw(10) << (procs->ki_rusage.ru_inblock)
                << "| " << setw(10) << (procs->ki_rusage.ru_oublock)
                << "| " << setw(4) << (procs->ki_numthreads)
                << "| " << setw(6) << ord(procs->ki_pri.pri_level)
                << endl;

            if (consoleOutput)
                cout << out.str();

            output += out.str();
            procs++;
        }

        kvm_close(kd);
        return output.c_str();
    }
} // extern


int main(int argc, char const *argv[]) {

    if (argc == 1) {
        cerr << "No UID argument given!" << endl;
        exit(NO_UID_GIVEN_ERROR);
    }

    getProcessUsage(atoi(argv[1]), true);
    return 0;
}
