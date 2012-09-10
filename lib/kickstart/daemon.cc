/**
 *  @author dmilith
 *
 *   Shell wrapper with UID given as argument to main.
 *   This helper is used by SSHD side of ServeD
 *
 */


#include "core.h"


#include <fcntl.h>
#include <paths.h>
#include <unistd.h>
#include <stdlib.h>


int spawnDaemon(int nochdir, int noclose) {
    int fd;

    switch (fork()) {
        case -1:
            return (-1);
        case 0:
            break;
        default:
            _exit(0);
    }

    if (setsid() == -1)
        return (-1);

    if (!nochdir)
        (void)chdir("/");

    if (!noclose && (fd = open(_PATH_DEVNULL, O_RDWR, 0)) != -1) {
        (void)dup2(fd, STDIN_FILENO);
        (void)dup2(fd, STDOUT_FILENO);
        (void)dup2(fd, STDERR_FILENO);
        if (fd > 2)
            (void)close(fd);
    }
    return (0);
}


void parse(char *line, char **argv) {
    while (*line != '\0') {
        while (*line == ' ' || *line == '\t' || *line == '\n') *line++ = '\0';
        *argv++ = line;
        while (*line != '\0' && *line != ' ' && *line != '\t' && *line != '\n') line++;
    }
    *argv = '\0';
}


void execute(char **argv, int uid) {
    stringstream hd, usr;
    hd << USERS_HOME_DIR << uid;
    usr << uid;
    chdir(hd.str().c_str());
    setenv("HOME", hd.str().c_str(), 1);
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
    spawnDaemon(0, 0);
    if (execvp(*argv, argv) < 0) {
        cerr << "Exec failed!" << endl;
        exit(EXEC_ERROR);
    }
}


int main(int argc, char const *argv[]) {
    cout << "ServeD Daemon v" << APP_VERSION << " - " << COPYRIGHT << endl;

    if (argc == 1) {
        cerr << "No UID argument given!" << endl;
        exit(NO_UID_GIVEN_ERROR);
    }

    string arg = string(argv[1]);
    if (arg == "0") {
        cerr << "Cannot spawn as root!" << endl;
        exit(ROOT_UID_ERROR);
    }

    /* Checking uid validity */
    uid_t uid;
    bool valid_uid = istringstream(arg) >> uid;
    if (!uid || !valid_uid) {
        cerr << "Ambigous uid given!" << endl;
        exit(AMBIGOUS_ENTRY_ERROR);
    }
    gid_t gid = DEFAULT_USER_GROUP;

    /* Checking home directory existnace */
    struct stat st;
    string homeDir = string(USERS_HOME_DIR) + arg; /* NOTE: /Users/$UID homedir format used here */
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
    for (int i = 2; i < argc; i++) {
        appArguments += string(argv[i]) + " ";
    }
    string command = string(DEFAULT_SHELL_COMMAND) + " -i -s";
    if (argc > 2) { // additional arguments => spawn custom command with uid privileges
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
