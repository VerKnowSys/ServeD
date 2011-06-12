/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "svd_wrap.h"


extern "C" {

    char* spawn(char* _command) {
        
        FILE            *fpipe = NULL;
        stringstream    ret;
        int             childExitStatus = 0;
        pid_t           pid;
        pid_t           ppid = getppid();
        char            line[256];
        
        if (!(fpipe = (FILE*)popen(_command, "r"))) {
            ret << POPEN_EXCEPTION;
            return (char*)(ret.str()).c_str();
        }
        while (fgets(line, sizeof(line) + 1, fpipe)) {
            ret << line << endl;
        }
        childExitStatus = pclose(fpipe);
        fpipe = NULL;
        
        if (childExitStatus == -1) {
            ret << CHILD_EXCEPTION;
            return (char*)(ret.str()).c_str();
        }
        
        // setbuf(stdout, NULL);
        // setbuf(stderr, NULL);
        pid = getpid();
        /* return string with "ParentPid;ChildProcessPid;UserID;CommandOutputFile" */
        ret << ppid << ";" << pid << endl;
        return (char*)(ret.str()).c_str();
        
    }

}
