/**
 *  @author dmilith
 *
 *   Application Runner.
 *   © 2011-2013 - VerKnowSys
 *
 */


#include "core.h"

#ifdef __FreeBSD__
#include <sys/wait.h>
#endif


extern void parse(char *line, char **argv);
extern int findpathof(char *pth, const char *exe);
extern vector<string> split(const string& s, const string& delim, const bool keep_empty = true);


void execute(char **argv, int uid, const char* env) {
    int status;
    pid_t  pid;
    if ((pid = fork()) < 0) {
        cerr << "Error forking child process failed!" << endl;
        exit(FORK_ERROR);
    } else if (pid == 0) {
        int fd;
        fd = open(_PATH_DEVNULL, O_RDWR, 0);
        fcntl(fd, F_SETFD, fcntl(fd, F_GETFD) | FD_CLOEXEC);

        stringstream hd, usr;
        hd << USERS_HOME_DIR << "/" << uid;
        usr << uid;
        chdir(hd.str().c_str());
        setenv("HOME", hd.str().c_str(), 1);
        setenv("PWD", hd.str().c_str(), 1);
        setenv("OLDPWD", hd.str().c_str(), 1);
        setenv("USER", usr.str().c_str(), 1);
        setenv("LOGNAME", usr.str().c_str(), 1);
        // setenv("LD_LIBRARY_PATH", env, 1); // most important env
        unsetenv("USERNAME");
        unsetenv("SUDO_USERNAME");
        unsetenv("SUDO_USER");
        unsetenv("SUDO_UID");
        unsetenv("SUDO_GID");
        unsetenv("SUDO_COMMAND");
        unsetenv("MAIL");
        if (execvp(*argv, argv) < 0) {
            cerr << "Exec failed!" << endl;
            exit(EXEC_ERROR);
        }
    } else
        while (wait(&status) != pid); /* wait for process */
}


int main(int argc, char const *argv[]) {

    cout << "ServeD Runner v" << APP_VERSION << " - " << COPYRIGHT << endl;
    int uid = getuid();

    if (argc == 1) {
        cerr << "No argument given!" << endl;
        exit(NO_UID_GIVEN_ERROR);
    }

    // count and gather arguments
    string appArguments = "";
    for (int i = 2; i < argc; i++) {
        if (NULL == argv[i+1])
            appArguments += string(argv[i]);
        else
            appArguments += string(argv[i]) + " ";
    }

    char path[PATH_MAX+1];
    const char *exe = argv[1];
    if (!findpathof(path, exe)) {
        fprintf(stderr, "No executable \"%s\" found\n", exe);
        return 1;
    }

    const vector<string> coreLibPath = split(path, string("exports/") + string(exe));
    string old_ld_library_path = "/lib:/usr/lib";
    string someLibDir = coreLibPath.front() + "lib/";
    string ld_library_path = old_ld_library_path;
    struct stat st;
    if(stat(someLibDir.c_str(), &st) == 0) { // change LD_LIBRARY_PATH when valid directory was found
        #ifdef DEVEL
            cerr << "Directory " << someLibDir << " does exists" << endl;
        #endif
        ld_library_path = someLibDir + ":" + old_ld_library_path;
    }
    #ifdef DEVEL
        else {
            cerr << "Directory " << someLibDir << " doesn't exists" << endl;
        }
    #endif

    #ifdef DEVEL
        cout << "Command Name: " << exe << endl;
        cout << "Command Executable Path: " << path << endl;
        cout << "Result LD_LIBRARY_PATH: " << ld_library_path << endl;
    #endif

    string command = string(path);
    if (argc > 2) { // additional arguments => spawn custom command
        command = string(path) + " " + appArguments;
    }
    #ifdef DEVEL
        cerr << "Full Command line: " << command << endl;
    #endif

    char *arguments[argc];
    parse((char*)(command.c_str()), arguments);
    execute(arguments, uid, ld_library_path.c_str());
}
