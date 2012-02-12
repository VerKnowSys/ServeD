/*
    Author: Daniel (dmilith) Dettlaff
    © 2012 - VerKnowSys
*/

#include "core.h"


#include <kvm.h>
#include <fcntl.h>
#include <sys/sysctl.h>
#include <sys/user.h>
#include <sys/proc.h>
#include <paths.h>
#include <stdlib.h>
#include <iomanip>

#define ord(c) ((int)(unsigned char)(c))


int getProcessUsage(int uid, bool consoleOutput) {

    int count = 0;
    char** args = NULL;
    string command;

    kvm_t* kd = kvm_open(NULL, "/dev/mem", NULL, O_RDONLY, "kvm_open");
    if (kd == 0) {
        cerr << "Error initializing kernel descriptor" << endl;
        return -1;
    }

    kinfo_proc* procs;
    // for (int g = 0; g < 100; g++)
        procs = kvm_getprocs(kd, KERN_PROC_UID, uid, &count); // get process directly from kernel

    if (consoleOutput)
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

    for (int i = 0; i < count; ++i) {
        command = "";
        args = kvm_getargv(kd, procs, 0);

        for (int y = 0; (args != 0) && (args[y] != 0); y++)
            if (y == 0)
                command = string(args[y]);
            else
                command += " " + string(args[y]);

        if (consoleOutput) {
            cout << setiosflags(ios::left)
                << "| " << setw(4) << (i + 1)
                << "| " << setw(25) << (procs->ki_comm)
                << "| " << setw(50) << (command)
                << "| " << setw(8) << (procs->ki_pid)
                << "| " << setw(8) << (procs->ki_ppid)
                << "| " << setw(8) << (procs->ki_rssize * 4)
                << "| " << setw(8) << (procs->ki_rusage.ru_maxrss * 4)
                << "| " << setw(14) << (procs->ki_runtime / 1000) // 1000000)
                << "| " << setw(10) << (procs->ki_rusage.ru_inblock)
                << "| " << setw(10) << (procs->ki_rusage.ru_oublock)
                // << "SWPS: " << (procs->ki_rusage.ru_nswap)
                << "| " << setw(4) << (procs->ki_numthreads)
                << "| " << setw(6) << ord(procs->ki_pri.pri_level)
                // << "PRI-NTVE: " << ord(procs->ki_pri.pri_native)
                // << "PRI-USER: " << ord(procs->ki_pri.pri_user)
                << endl;
        }

        procs++;
    }

    kvm_close(kd);
    return 0;
}


int main(int argc, char const *argv[]) {
    getProcessUsage(0, true);
    return 0;
}

