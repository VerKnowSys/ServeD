/**
 *  @author tallica
 *
 *   © 2011-2013 - VerKnowSys
 *
 */


#include "../globals/globals.h"
#include "config_loader.h"


#include <QtCore>


class SandboxProcess: public QProcess {

public:
    SandboxProcess(int uid) {
        this->uid = uid;
        setProcessChannelMode(MergedChannels);
        setStandardOutputFile("/SystemUsers/service_spawner.log", QIODevice::Append);
    }

    void spawnDefaultShell() {
        qDebug() << "Spawning default shell for uid:" << uid;
        start(QString(DEFAULT_SHELL_COMMAND), QStringList() << "-s");
    }

    void spawnService(const char *command) {
        spawnDefaultShell();
        qDebug() << "Spawning command" << QString(command) << "for uid:" << uid;
        write(command);
        closeWriteChannel();
    }


protected:
    void setupChildProcess() {
        qDebug() << "Setup process environment";
        stringstream hd, usr;

        if (uid == 0)
            hd << SYSTEMUSERS_HOME_DIR;
        else
            hd << USERS_HOME_DIR << uid;

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
    QString stopPath;
    QString spawnedPath;
    QFile spawnFile;
    QFile stopFile;
    QFile spawnedFile;

    SvdEventFiles(const QString & path, const QString & name, const QString & prefix = ".") {
        spawnPath   = path + "/" + prefix + "spawn_" + name;
        stopPath    = path + "/" + prefix + "stop_" + name;
        spawnedPath = path + "/" + prefix + "spawned_" + name;
        spawnFile.setFileName(spawnPath);
        stopFile.setFileName(stopPath);
        spawnedFile.setFileName(spawnedPath);
    }

};


class SvdEventFilesWatcher: public QFileSystemWatcher {

public:
    void registerFile(const QString & path) {
        qDebug() << "Registering file:" << path;
        addPath(path);
    }


    void unregisterFile(const QString & path) {
        qDebug() << "Unregistering file:" << path;
        removePath(path);
    }


    bool isWatchingFile(const QString & path) {
        return files().contains(path);
    }


    bool isWatchingDir(const QString & path) {
        return directories().contains(path);
    }
};


void setHomeDir(int uid, QString & homeDir) {
    if (uid == 0)
        homeDir = QString::fromStdString(SYSTEMUSERS_HOME_DIR);
    else
        homeDir = QString::fromStdString(USERS_HOME_DIR) + QString::number(uid);
}


class SvdUserWatcher: public QObject
{
    Q_OBJECT


public:
    SvdUserWatcher(int uid) {
        qDebug() << "Starting SvdUserWatcher for uid:" << uid;

        setHomeDir(uid, homeDir);
        this->uid = uid;

        loop = new QEventLoop();

        cleanupEventFiles();

        fileWatcher = new SvdEventFilesWatcher;
        fileWatcher->registerFile(homeDir);

        /* Check for event files at startup */
        dirChangedSlot(homeDir);

        connect(fileWatcher, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
        connect(fileWatcher, SIGNAL(fileChanged(QString)), this, SLOT(fileChangedSlot(QString)));

        loop->exec();
    }

private:
    int uid;
    QString homeDir;
    QEventLoop *loop;
    SvdEventFilesWatcher *fileWatcher;
    QList<SandboxProcess *> spawnedServices;

    void cleanupEventFiles() {
        SvdEventFiles userPanel(homeDir, "user_panel");
        userPanel.stopFile.remove();
        userPanel.spawnedFile.remove();
    }


signals:
    void spawnUserPanel(int uid);
    void stopUserPanel(int uid);
    void spawnedUserPanel(int uid);
    void stoppedUserPanel(int uid);


public slots:
    void dirChangedSlot(const QString& str) {
        QFile file;

        if (str.compare(homeDir) == 0) {
            SvdEventFiles userPanel(homeDir, "user_panel");

            if (userPanel.spawnFile.exists()) {
                userPanel.spawnFile.remove();
                if (userPanel.spawnedFile.exists())
                    qDebug() << "Interrupted emission of spawnUserPanel signal. User panel is already running for uid:" << uid;
                else {
                    qDebug() << "Emitting spawnUserPanel signal for uid:" << uid;
                    emit spawnUserPanel(uid);
                }
                return;
            }

            if (userPanel.stopFile.exists()) {
                userPanel.stopFile.remove();
                if (userPanel.spawnedFile.exists()) {
                    qDebug() << "Emitting stopUserPanel signal for uid:" << uid;
                    emit stopUserPanel(uid);
                } else
                    qDebug() << "Interrupted emission of stopUserPanel signal. User panel is not running for uid:" << uid;
                return;
            }

            if (userPanel.spawnedFile.exists() && not fileWatcher->isWatchingFile(userPanel.spawnedPath)) {
                qDebug() << "Emitting spawnedUserPanel signal for uid:" << uid;
                fileWatcher->registerFile(userPanel.spawnedPath);
                emit spawnedUserPanel(uid);
                return;
            }
        }
    }


    void fileChangedSlot(const QString& str) {
        qDebug() << "File changed:" << str;

        SvdEventFiles userPanel(homeDir, "user_panel");

        if (str.compare(userPanel.spawnedPath) == 0 && not userPanel.spawnedFile.exists()) {
            qDebug() << "Emitting stoppedUserPanel signal for uid:" << uid;
            emit stoppedUserPanel(uid);
        }
    }
};


void spawnSvdUserWatcher(int uid) {
    SvdUserWatcher userWatcher(uid);
}


int main(int argc, char *argv[]) {

    QCoreApplication app(argc, argv);
    QList<int> userIdsToWatch;
    QFutureWatcher<void> watcher;


    SvdServiceConfig *config = new SvdServiceConfig("RedisDrugi");
    cout << "Igniter name element: " << config->name.toStdString() << endl;
    cout << "Igniter softwareName element: " << config->softwareName.toStdString() << endl;
    cout << "Igniter install element: " << config->install->commands.toStdString() << endl;
    cout << "Igniter start element: " << config->start->commands.toStdString() << endl;
    cout << "Igniter stop element: " << config->stop->commands.toStdString() << endl;
    cout << "Igniter autoStart element: " << config->autoStart << endl;
    cout << "Igniter staticPort element: " << config->staticPort << endl;
    cout << "Igniter watchPort element: " << config->watchPort << endl;
    cout << "Igniter schedulerActions element: " << config->schedulerActions.first() << endl;

    SvdServiceConfig *config2 = new SvdServiceConfig("Nginx");
    cout << "Igniter2 name element: " << config2->name.toStdString() << endl;
    cout << "Igniter2 softwareName element: " << config2->softwareName.toStdString() << endl;
    cout << "Igniter2 install element: " << config2->install->commands.toStdString() << endl;
    cout << "Igniter2 start element: " << config2->start->commands.toStdString() << endl;
    cout << "Igniter2 stop element: " << config2->stop->commands.toStdString() << endl;
    cout << "Igniter2 autoStart element: " << config2->autoStart << endl;
    cout << "Igniter2 staticPort element: " << config2->staticPort << endl;
    cout << "Igniter2 watchPort element: " << config2->watchPort << endl;
    cout << "Igniter2 schedulerActions element: " << config2->schedulerActions.first() << endl;

    cout << "Quitting!" << endl;
    exit(0);


    QFile file("/SystemUsers/service_spawner.log");
    file.remove();

    // XXX
    userIdsToWatch << 500 << 501 << 1000;


    QFuture<void> result = QtConcurrent::map(userIdsToWatch, spawnSvdUserWatcher);
    watcher.setFuture(result);

    return app.exec();
}

#include "service_spawner.moc"
