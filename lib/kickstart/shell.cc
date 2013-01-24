/**
 *  @author dmilith, tallica
 *
 *   Shell wrapper with UID given as argument to main.
 *   This helper is used by SSHD side of ServeD
 *   © 2011-2013 - VerKnowSys
 *
 */


#include "core.h"

#ifdef __FreeBSD__
#include <sys/wait.h>
#endif


void execute(char **argv, const string& command, int uid) {
    int master;
    pid_t pid;
    struct winsize w = {
        .ws_col = 80,
        .ws_row = 30
    };

    /* Get the current size of the terminal */
    if (isatty(STDOUT_FILENO))
        ioctl(STDOUT_FILENO, TIOCGWINSZ, &w);

    if ((pid = forkpty(&master, NULL, NULL, &w)) < 0) {
        cerr << "Error forking child process failed!" << endl;
        exit(FORK_ERROR);
    } else if (pid == 0) {
        stringstream hd, usr;

        if (uid == 0)
            hd << SYSTEMUSERS_HOME_DIR;
        else
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
    } else {
        /* Adapted from https://gist.github.com/3547195 */

        // remove the echo
        struct termios tios;
        tcgetattr(master, &tios);
        tios.c_lflag &= ~(ECHO | ECHONL);
        tcsetattr(master, TCSAFLUSH, &tios);

        /* Execute custom command */
        if (command.length() > 0)
            write(master, command.c_str(), command.length());

        for (;;) {
            fd_set read_fd;
            FD_ZERO(&read_fd);

            FD_SET(master, &read_fd);
            FD_SET(STDIN_FILENO, &read_fd);

            select(master+1, &read_fd, NULL, NULL, NULL);

            char input;
            char output;

            if (FD_ISSET(master, &read_fd))
            {
                if (read(master, &output, 1) != -1)
                    write(STDOUT_FILENO, &output, 1);
                else
                    break;
            }

            if (FD_ISSET(STDIN_FILENO, &read_fd))
            {
                read(STDIN_FILENO, &input, 1);
                write(master, &input, 1);
            }
        }
    }
}


static void printVersion(void) {
    cout << "ServeD Shell v" << APP_VERSION << " - " << COPYRIGHT << endl;
}


static void printUsage(void) {
    cout << endl;
    cout << "Usage: " << endl;
    cout << "  svdshell [option] [command]" << endl;
    cout << "  svdshell --uid=700 pstree" << endl;
    cout << "  svdshell -u700 -- ls -la" << endl;
    cout << endl;
    cout << "Options:" << endl;
    cout << "  -h, --help         This message." << endl;
    cout << "  -u, --uid=<uid>    Spawn command with <uid> privileges." << endl;
    cout << "  -v, --version      Show copyright and version information." << endl;
}


int main(int argc, char *argv[]) {

    const char *defShell[] = {DEFAULT_SHELL_COMMAND, "-s", NULL};
    string command;
    int opt = 0;

    printVersion();

    uid_t uid = getuid();
    gid_t gid = DEFAULT_USER_GROUP;

    /* Available options */
    static struct option options[] = {
        {"help", no_argument, 0, 'h'},
        {"uid", optional_argument, 0, 'u'},
        {"version", no_argument, 0, 'v'},
        {NULL, 0, NULL, 0}
    };

    while ((opt = getopt_long(argc, argv, "hu:v", options, NULL)) != -1) {
        switch (opt) {
            case 'h':
                printUsage();
                exit(EXIT_SUCCESS);
            case 'u': {
                    if (uid != 0) {
                        cerr << "You are not allowed to specify custom uid!" << endl;
                        exit(EXIT_FAILURE);
                    }

                    int optUid;
                    if (!optarg || !(istringstream(optarg) >> optUid) || optUid < 0) {
                        cerr << "Ambigous uid given!" << endl;
                        exit(AMBIGOUS_ENTRY_ERROR);
                    } else
                        uid = optUid;
                }
                break;
            case 'v':
                exit(EXIT_SUCCESS);
            case '?':
            case ':':
                exit(EXIT_FAILURE);
            default:
                break;
        }
    }

    /* Checking for additional arguments */
    if (optind < argc) {
        char **args = argv + optind;
        stringstream ss;
        for (int i = 0; args[i] != NULL; i++) {
            if (args[i + 1] != NULL)
                ss << args[i] << " ";
            else
                ss << args[i] << endl;
        }
        command = ss.str();
    }

    /* Checking home directory existnace */
    struct stat st;
    string homeDir;

    if (uid == 0)
        homeDir = string(SYSTEMUSERS_HOME_DIR);
    else {
        stringstream ss;
        ss << string(USERS_HOME_DIR) << uid;
        homeDir = ss.str(); /* NOTE: /Users/$UID homedir format used here */
    }

    if (stat(homeDir.c_str(), &st) == 0) {
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
    #ifndef __APPLE__
        if (setgid(gid) != 0) {
            cerr << "Error setgid to gid: " << gid << endl;
            exit(SETGID_ERROR);
        }
    #endif
    // chdir(homeDir.c_str());

    #ifdef DEVEL
        cerr << "Spawning command for uid: " << uid << ", gid: " << gid << endl;
        if (command.length() > 0)
            cerr << "Command line: " << command;
    #endif

    execute((char **) defShell, command, uid);
}
