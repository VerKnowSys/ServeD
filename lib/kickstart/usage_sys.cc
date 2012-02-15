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


    const char* getProcessUsage(int uid, bool consoleOutput) {

        int count = 0;
        char** args = NULL;
        string command, output;

        kvm_t* kd = kvm_open(NULL, "/dev/null", NULL, O_RDONLY, NULL);
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

            if (consoleOutput) {
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
            } else {
                out << (procs->ki_pid)
                    << "|" << (procs->ki_ppid)
                    << "|" << (procs->ki_comm)
                    << "|" << (command)
                    << "|" << (procs->ki_rssize * 4)
                    << "|" << (procs->ki_rusage.ru_maxrss * 4)
                    << "|" << (procs->ki_runtime / 1000)
                    << "|" << (procs->ki_rusage.ru_inblock)
                    << "|" << (procs->ki_rusage.ru_oublock)
                    << "|" << (procs->ki_numthreads)
                    << "|" << ord(procs->ki_pri.pri_level)
                    << endl;
            }

            args = NULL;
            output += out.str();
            procs++;
        }

        kvm_close(kd);
        return output.c_str();
    }


} // extern


