/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "process.h"


SvdProcess::SvdProcess(const QString& serviceName, const QString& prefix) {
    setProcessChannelMode(MergedChannels);
    setupChildProcess();
    // XXX:
    setStandardOutputFile(getSoftwareDataDir() + "/" + serviceName + "/."+ prefix + ".stdout.log", QIODevice::Append);
    setStandardErrorFile(getSoftwareDataDir() + "/" + serviceName + "/."+ prefix + ".stderr.log", QIODevice::Append);
}


void SvdProcess::spawnDefaultShell() {
    logDebug() << "Spawning default shell.";
    start(QString(DEFAULT_SHELL_COMMAND), QStringList());
}


void SvdProcess::spawnProcess(const QString& command) {
    spawnDefaultShell();
    logDebug() << "Spawning command:" << QString(command);
    write(command.toUtf8());
    closeWriteChannel();
    // stop();
}


void SvdProcess::setupChildProcess() {
    uid_t uid = getuid();

    const QString home = getHomeDir();
    const QString user = QString::number(uid).toUtf8();
    logDebug() << "Setup process environment with home:" << home << "and user:" << user;

    #ifdef __FreeBSD__
        setgroups(0, 0);
    #endif
    setuid(uid);
    chdir(home.toUtf8());
    setenv("HOME", home.toUtf8(), 1);
    setenv("~", home.toUtf8(), 1);
    setenv("PWD", home.toUtf8(), 1);
    setenv("OLDPWD", home.toUtf8(), 1);
    setenv("USER", user.toUtf8(), 1);
    setenv("LOGNAME", user.toUtf8(), 1);
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