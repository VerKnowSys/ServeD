/**
 *  @author tallica
 *
 *   © 2011-2013 - VerKnowSys
 *
 */


#include "../globals/globals.h"
#include "service_config.h"
#include "utils.h"

#include <QtCore>


// XXX: REMOVE THOSE:
#include <iostream>
#include <sstream>
// EOF XXX


/*
    TODO:
    * Split classes into seperate files.
*/


class SandboxProcess: public QProcess {

public:
    SandboxProcess(int uid) {
        this->uid = uid;
        setProcessChannelMode(MergedChannels);
        setStandardOutputFile("/SystemUsers/service_spawner.log", QIODevice::Append);
    }

    void spawnDefaultShell() {
        logDebug() << "Spawning default shell for uid:" << uid;
        start(QString(DEFAULT_SHELL_COMMAND), QStringList() << "-s");
    }

    void spawnService(const char *command) {
        spawnDefaultShell();
        logDebug() << "Spawning command" << QString(command) << "for uid:" << uid;
        write(command);
        closeWriteChannel();
    }


protected:
    void setupChildProcess() {
        logDebug() << "Setup process environment";
        stringstream hd, usr;

        if (uid == 0)
            hd << SYSTEMUSERS_HOME_DIR;
        else
            hd << USERS_HOME_DIR << "/" << uid;

        usr << uid;

        const char *homeDir = hd.str().c_str();
        const char *user = usr.str().c_str();

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


private:
    int uid;
};


class SvdEventFiles {

public:
    QString spawnPath;
    QString terminatePath;
    QString spawnedPath;
    QFile spawnFile;
    QFile terminateFile;
    QFile spawnedFile;

    SvdEventFiles(const QString & path) {
        spawnPath     = path + "/.spawn";
        terminatePath = path + "/.terminate";
        spawnedPath   = path + "/.spawned";
        spawnFile.setFileName(spawnPath);
        terminateFile.setFileName(terminatePath);
        spawnedFile.setFileName(spawnedPath);
    }

};


class SvdEventFilesWatcher: public QFileSystemWatcher {

public:
    void registerFile(const QString & path) {
        logDebug() << "Registering file:" << path;
        addPath(path);
    }


    void unregisterFile(const QString & path) {
        logDebug() << "Unregistering file:" << path;
        removePath(path);
    }


    bool isWatchingFile(const QString & path) {
        return files().contains(path);
    }


    bool isWatchingDir(const QString & path) {
        return directories().contains(path);
    }
};





class SvdServiceWatcher: public QObject
{
    Q_OBJECT


public:
    SvdServiceWatcher(const QString & name) {
        logDebug() << "Starting SvdServiceWatcher for service:" << name;

        setServiceDataDir(dataDir, name);

        loop = new QEventLoop();

        config = new SvdServiceConfig(name);

        fileWatcher = new SvdEventFilesWatcher;
        fileWatcher->registerFile(dataDir);

        eventFiles = new SvdEventFiles(dataDir);

        /* Not sure if necessary */
        cleanupEventFiles();

        connect(fileWatcher, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
        connect(fileWatcher, SIGNAL(fileChanged(QString)), this, SLOT(fileChangedSlot(QString)));

        loop->exec();
    }

private:
    QEventLoop *loop;
    SvdServiceConfig *config;
    SvdEventFilesWatcher *fileWatcher;
    SvdEventFiles *eventFiles;
    QList<SandboxProcess *> spawnedServices;
    QString dataDir;

    void cleanupEventFiles() {
        eventFiles->terminateFile.remove();
        eventFiles->spawnedFile.remove();
    }


signals:
    void spawnService(const SvdServiceConfig & config);
    void terminateService(const SvdServiceConfig & config);
    void spawnedService(const SvdServiceConfig & config);
    void terminatedService(const SvdServiceConfig & config);


public slots:
    void dirChangedSlot(const QString & dir) {
        // logDebug() << "Directory changed:" << dir;

        if (eventFiles->spawnFile.exists()) {
            eventFiles->spawnFile.remove();
            if (eventFiles->spawnedFile.exists())
                logDebug() << "Interrupted emission of spawnService(" << config->name << ") signal. Service is already running.";
            else {
                logDebug() << "Emitting spawnService(" << config->name << ") signal.";
                emit spawnService(*config);
            }
            return;
        }

        if (eventFiles->terminateFile.exists()) {
            eventFiles->terminateFile.remove();
            if (eventFiles->spawnedFile.exists()) {
                logDebug() << "Emitting terminateService(" << config->name << ") signal.";
                emit terminateService(*config);
            } else
                logDebug() << "Interrupted emission of terminateService(" << config->name << ") signal. Service is not running.";
            return;
        }

        if (eventFiles->spawnedFile.exists() && not fileWatcher->isWatchingFile(eventFiles->spawnedPath)) {
            logDebug() << "Emitting spawnedService(" << config->name << ") signal.";
            fileWatcher->registerFile(eventFiles->spawnedPath);
            emit spawnedService(*config);
            return;
        }
    }


    void fileChangedSlot(const QString & file) {
        logDebug() << "File changed:" << file;

        if (file.compare(eventFiles->spawnedPath) == 0 && not eventFiles->spawnedFile.exists()) {
            logDebug() << "Emitting terminatedService(" << config->name << ") signal.";
            emit terminatedService(*config);
        }
    }
};


void spawnSvdServiceWatcher(const QString & name) {
    SvdServiceWatcher serviceWatcher(name);
}


int main(int argc, char *argv[]) {

    QCoreApplication app(argc, argv);
    QFutureWatcher<void> watcher;
    QString softwareDataDir;
    QStringList services;

    /* Logger setup */
    ConsoleAppender *consoleAppender = new ConsoleAppender();
    consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] <%c> %m\n");
    Logger::registerAppender(consoleAppender);
    QTextCodec::setCodecForCStrings(QTextCodec::codecForName("utf8"));

    logInfo() << "Starting Service Spawner for uid:" << getuid();

    // XXX
    QFile file("/SystemUsers/service_spawner.log");
    file.remove();

    setSoftwareDataDir(softwareDataDir);

    logDebug() << "Looking for services inside" << softwareDataDir;
    services = QDir(softwareDataDir).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);

    Q_FOREACH(QString name, services)
        logDebug() << "Found" << name << "service.";


    QFuture<void> result = QtConcurrent::map(services, spawnSvdServiceWatcher);
    watcher.setFuture(result);

    return app.exec();
}

#include "service_spawner.moc"
