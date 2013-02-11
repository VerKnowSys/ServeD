/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "process.h"


SvdProcess::SvdProcess() {
    setProcessChannelMode(MergedChannels);
    // XXX
    // setStandardOutputFile("/Users/UID/SoftwareData/SERVICE/process.log", QIODevice::Append);
}


void SvdProcess::spawnDefaultShell() {
    logDebug() << "Spawning default shell.";
    start(QString(DEFAULT_SHELL_COMMAND), QStringList() << "-s");
}


void SvdProcess::spawnService(const char *command) {
    spawnDefaultShell();
    logDebug() << "Spawning command:" << QString(command);
    write(command);
    closeWriteChannel();
}


void SvdProcess::setupChildProcess() {
    logDebug() << "Setup process environment.";
    uid_t uid = getuid();

    const char *homeDir = getHomeDir().toStdString().c_str();
    const char *user = QString::number(uid).toStdString().c_str();

    #ifdef __FreeBSD__
        setgroups(0, 0);
    #endif
    setuid(uid);
    chdir(homeDir);
    setenv("HOME", homeDir, 1);
    setenv("~", homeDir, 1);
    setenv("PWD", homeDir, 1);
    setenv("OLDPWD", homeDir, 1);
    setenv("USER", user, 1);
    setenv("LOGNAME", user, 1);
    setenv("LC_ALL", LOCALE, 1);
    setenv("LANG", LOCALE, 1);
    unsetenv("USERNAME");
    unsetenv("SUDO_USERNAME");
    unsetenv("SUDO_USER");
    unsetenv("SUDO_UID");
    unsetenv("SUDO_GID");
    unsetenv("SUDO_COMMAND");
    unsetenv("MAIL");
}