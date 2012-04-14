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


void execute(char **argv, int uid) {
    int status;
    pid_t  pid;
    if ((pid = fork()) < 0) {
        cerr << "Error forking child process failed!" << endl;
        exit(FORK_ERROR);
    } else if (pid == 0) {
        stringstream hd, usr;
        hd << USERS_HOME_DIR << uid;
        usr << uid;
        const char* homeDir = hd.str().c_str();
        const char* userName = usr.str().c_str();
        chdir(homeDir);
        setenv("HOME", hd.str().c_str(), 1);
        setenv("LOGNAME", userName, 1);
        setenv("PWD", homeDir, 1);
        setenv("OLDPWD", homeDir, 1);
        setenv("USER", userName, 1);
        setenv("USERNAME", userName, 1);
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
    uid_t uid = atoi(arg.c_str());
    if (!uid && arg != "0") {
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

    string command = string(DEFAULT_SHELL_COMMAND) + " -i -s";
    #ifdef DEVEL
        cerr << "Spawning command: " << command << ", for uid: " << uid << " and gid: " << gid << endl;
    #endif

    char *arguments[2];
    parse((char*)(command.c_str()), arguments);
    #ifdef DEVEL
        cout << "Arguments: ";
        for (int i = 0; arguments[i] != NULL; ++i)
            cout << arguments[i] << " ";
        cout << endl;
    #endif
    execute(arguments, uid);
}
