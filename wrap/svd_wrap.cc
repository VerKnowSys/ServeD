/* 
    Author: Daniel (dmilith) Dettlaff
    © 2011 - VerKnowSys
*/


#include "svd_wrap.h"


string spawn(int user_uid, string command, string output_file) {

    pid_t           pid = fork();
    pid_t           ppid = getppid();
    stringstream    ret;
    
    /* set effective user to spawn process as */
    if (setuid(user_uid) != 0) {
        ret << SETUID_EXCEPTION;
        return ret.str();
    }
    
    if (pid == 0) { /* child */
        
        /* redirect stdout & stderr to output_file */
        close(1);
        close(2);
        open(output_file.c_str(), O_WRONLY);
        open(output_file.c_str(), O_WRONLY);
        
        /* touch output file */
        ofstream of(output_file.c_str());
        of.close();
        
        /* create command vector */
        istringstream iss(command);
        vector<string> cmd;
        copy(
            istream_iterator<string>(iss),
            istream_iterator<string>(),
            back_inserter<vector<string> >(cmd)
        );
        
        /* spawn child */
        execvp(cmd[0].c_str(), (char**)(&cmd[0])); /* execvp will use PATH environment defined for user spawing process */
        
        /* execvp will never reach this code if everything is fine */
        ret << EXECVP_EXCEPTION;
        return ret.str();
        
    } else if (pid < 0) {
        cerr << "Failed to fork" << endl;
        ret << FORK_EXCEPTION;
        return ret.str();
        
    } else {
        /* do nothing in parent */
    }

    /* return string with "ParentPid;ChildProcessPid;UserID;CommandOutputFile" */
    ret << ppid << ";" << pid << ";" << user_uid << ";" << output_file;
    return ret.str();
}


/* example in case of standalone building (no dylib/so mode) */
int main() {
    // cout << spawn(75, "/bin/cat /var/log/kernel.log", "/tmp/dupaout") << endl; // NOTE: this wont work cause uid 75 don't have access to /var/log/kernel.log
    cout << spawn(75, "/bin/sleep 50", "/dev/null") << endl;
    return 0;
}
