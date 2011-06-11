/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "svd_wrap.h"


extern "C" {

    char* spawn(int user_uid, char* _command, char* _output_file) {
        
        bool            child = false;
        FILE            *fpipe = NULL;
        stringstream    ret;
        int             childExitStatus = 0;
        pid_t           pid;
        pid_t           ppid = getppid();
        char            line[256];
        
        if (child == false) 
            pid = fork();
            
        if (pid == 0) { /* child */
            
            /* avoid child having children - use condoms! */
            child = true;
            
            /* redirect output to _output_file */
            close(1);
            close(2);
            freopen(_output_file, "w", stdout);
            freopen(_output_file, "w", stderr);
                    
            /* set effective user to spawn child with given uid */
            if (setuid(user_uid) != 0) {
                ret << SETUID_EXCEPTION;
                return (char*)(ret.str()).c_str();
            }
            
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
        
        } else if (pid < 0) {
            ret << FORK_EXCEPTION;
            return (char*)(ret.str()).c_str();
        
        } else {
        }

        setbuf(stdout, NULL);
        setbuf(stderr, NULL);
        fpipe = NULL;
        
        /* return string with "ParentPid;ChildProcessPid;UserID;CommandOutputFile" */
        ret << ppid << ";" << pid << ";" << user_uid << ";" << string(_output_file);
        return (char*)(ret.str()).c_str();
        
    }

}
