/**
 *  @author dmilith
 *
 *   Shell wrapper with UID given as argument to main.
 *   This helper is used by SSHD side of ServeD
 *
 */


#include "core.h"

#ifdef __FreeBSD__
#include <sys/wait.h>
#endif


void parse(char *line, char **argv) {
    while (*line != '\0') {
        while (*line == ' ' || *line == '\t' || *line == '\n') *line++ = '\0';
        *argv++ = line;
        while (*line != '\0' && *line != ' ' && *line != '\t' && *line != '\n') line++;
    }
    *argv = '\0';
}

     
void execute(char **argv) {
    int    status;
    pid_t  pid;
    if ((pid = fork()) < 0) {
        cerr << "Error forking child process failed!" << endl;
        exit(FORK_ERROR);
    } else if (pid == 0) {
        if (execvp(*argv, argv) < 0) {
            cerr << "Exec failed!" << endl;
            exit(EXEC_ERROR);
        }
    } else {
        while (wait(&status) != pid); /* wait for process */
    }
}


int main(int argc, char const *argv[]) {
        
    char  *arguments[64];
    string arg, homeDir;
    if (argc == 1) {
        cerr << "No UID argument given!" << endl;
        exit(NO_UID_GIVEN_ERROR);
    }
    arg = string(argv[1]);
    if (arg == "0") {
        cerr << "Cannot spawn as root!" << endl;
        exit(1);
    }
    
    homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format */
    #ifdef DEVEL
        cerr << "Spawning user shell for UID: " << arg << endl;
    #endif

    uid_t uid = atoi(arg.c_str());
    chdir(homeDir.c_str());
    if (setuid(uid) != 0) {
        cerr << "Error setuid to uid: " << uid << endl;
        exit(SETUID_ERROR);
    }

    string command = string(DEFAULT_SHELL_COMMAND) + " -i -s";
    #ifdef DEVEL
        cerr << "Spawning shell: " << command << endl;
    #endif
    
    char* cmd = (char*)(command.c_str());
    parse(cmd, arguments);
    execute(arguments);
}
