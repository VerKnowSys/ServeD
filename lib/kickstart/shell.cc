/**
 *  @author dmilith
 *
 *   Shell wrapper with UID given as argument to main.
 *   This helper is used by SSHD side of ServeD
 *   © 2011-2012 - VerKnowSys
 *
 */


#include "core.h"

#ifdef __FreeBSD__
#include <sys/wait.h>
#endif


extern void parse(char *line, char **argv);


void execute(char **argv, int uid) {
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
        hd << USERS_HOME_DIR << uid;
        usr << uid;
        chdir(hd.str().c_str());
        setenv("HOME", hd.str().c_str(), 1);
        setenv("~", hd.str().c_str(), 1);
        setenv("PWD", hd.str().c_str(), 1);
        setenv("OLDPWD", hd.str().c_str(), 1);
        setenv("USER", usr.str().c_str(), 1);
        setenv("LOGNAME", usr.str().c_str(), 1);
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

    cout << "ServeD Shell v" << APP_VERSION << " - " << COPYRIGHT << endl;

    /* Print motd */
    ifstream t(MOTD_FILE);
    stringstream buffer;
    buffer << t.rdbuf();
    if (buffer.str() != "") {
        cout << endl << buffer.str() << endl;
    }

    uid_t uid = getuid();
    gid_t gid = DEFAULT_USER_GROUP;

    /* Checking home directory existnace */
    struct stat st;
    stringstream ss;
    ss << string(USERS_HOME_DIR) << uid;
    string homeDir = ss.str(); /* NOTE: /Users/$UID homedir format used here */
    if(stat(homeDir.c_str(), &st) == 0) {
        #ifdef DEVEL
            cerr << "Home directory " << homeDir << " is present" << endl;
        #endif
    } else {
        #ifdef DEVEL
            cerr << "Creating home directory " <<
                homeDir << " and chowning it for uid:" <<
                    uid << " and gid: " <<
                        gid << endl;
            mkdir(homeDir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        #else
            mkdir(homeDir.c_str(), S_IRWXU | S_IXOTH); /* No rights for others than user - most safe */
        #endif
        chown(homeDir.c_str(), uid, gid);
    }

    /* setting user uid and gid privileges for shell */
    if (setuid(uid) != 0) {
        cerr << "Error setuid to uid: " << uid << endl;
        exit(SETUID_ERROR);
    }
    if (setgid(gid) != 0) {
        cerr << "Error setgid to gid: " << gid << endl;
        exit(SETGID_ERROR);
    }
    // chdir(homeDir.c_str());

    // count and gather arguments
    string appArguments = "";
    for (int i = 1; i < argc; i++) {
        if (NULL == argv[i+1])
            appArguments += string(argv[i]);
        else
            appArguments += string(argv[i]) + " ";
    }
    string command = string(DEFAULT_SHELL_COMMAND) + " -i -s";
    if (argc > 1) { // additional arguments => spawn custom command with uid privileges
        command = appArguments;
    }
    #ifdef DEVEL
        cerr << "Spawning command for uid: " << uid << ", gid: " << gid << endl;
        cerr << "Command line: " << command << endl;
    #endif

    char *arguments[argc];
    parse((char*)(command.c_str()), arguments);
    execute(arguments, uid);
}
