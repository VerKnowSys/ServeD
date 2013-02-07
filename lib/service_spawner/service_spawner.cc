/**
 *  @author tallica
 *
 *   © 2011-2013 - VerKnowSys
 *
 */


#include "../globals/globals.h"
#include "config_loader.h"

#include <QCoreApplication>
#include <QFileSystemWatcher>
#include <QObject>
#include <QDebug>
#include <QStringList>
#include <QProcess>
#include <QFile>



class SandboxProcess: public QProcess
{
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


class UserWatcher: public QObject
{
    Q_OBJECT


public:
    UserWatcher(int uid) {
        qDebug() << "Starting UserWatcher for uid:" << uid;

        if (uid == 0)
            homeDir = QString::fromStdString(SYSTEMUSERS_HOME_DIR);
        else
            homeDir = QString::fromStdString(USERS_HOME_DIR) + QString::number(uid);

        this->uid = uid;

        watcher.addPath(homeDir);
        connect(&watcher, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
    }

public slots:
    void dirChangedSlot(const QString& str) {
        qDebug() << "Directory changed:" << str;
        // QFile file(str + "/.spawn_user_panel");
        // if (file.exists())
        //     qDebug() << "Spawning UserPanel for:" << uid;
    }

    void fileChangedSlot(const QString& str) {
        qDebug() << "File changed:" << str;
    }

private:
    int uid;
    QString homeDir;
    QFileSystemWatcher watcher;
    QList<SandboxProcess *> spawnedServices;
    QEventLoop *loop;
};


int main(int argc, char *argv[]) {

    QCoreApplication app(argc, argv);
    QList<int> userIdsToWatch;
    QList<UserWatcher *> watchers;

    SvdServiceConfig *config = new SvdServiceConfig("Redis");
    cout << "Igniter softwareName element: " << config->softwareName.toStdString() << endl;
    cout << "Igniter install element: " << config->install->commands.toStdString() << endl;
    cout << "Igniter start element: " << config->start->commands.toStdString() << endl;
    cout << "Igniter stop element: " << config->stop->commands.toStdString() << endl;
    cout << "Igniter autoStart element: " << config->autoStart << endl;
    cout << "Igniter staticPort element: " << config->staticPort << endl;
    cout << "Igniter watchPort element: " << config->watchPort << endl;
    cout << "Igniter schedulerActions element: " << config->schedulerActions.first() << endl;

    SvdServiceConfig *config2 = new SvdServiceConfig("Nginx");
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

    Q_FOREACH(int uid, userIdsToWatch) {
        watchers << new UserWatcher(uid);
    }

    // XXX
    SandboxProcess proc(501);
    proc.spawnService("redis-server");

    SandboxProcess proc2(500);
    proc2.spawnService("redis-server --port 1234");

    return app.exec();
}

#include "service_spawner.moc"
