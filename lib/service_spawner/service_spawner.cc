/**
 *  @author tallica, dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "logger.h"
#include "../globals/globals.h"
#include "service_config.h"
#include "service_watcher.h"
#include "user_watcher.h"
#include "utils.h"

#include <QtCore>


void spawnSSForEachUser() {
    auto userDirs = QDir(USERS_HOME_DIR).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);
    QList<int> dirs;

    /* filter through invalid directories */
    Q_FOREACH(QString directory, userDirs) {
        bool ok;
        int validUserDir = directory.toInt(&ok, 10); /* valid user directory must be number here */
        if (ok)
            dirs << validUserDir;
        else
            logTrace() << "Filtering out userDir:" << directory;
    }

    /* spawn ss for each of uids in /Users */
    Q_FOREACH(int directory, dirs) {
        logDebug() << "Spawning user SS for:" << QString::number(directory);

        auto proc = new SvdProcess("SS", directory, false); // don't redirect output
        proc->spawnProcess(DEFAULT_SS_COMMAND);
    }
}


int main(int argc, char *argv[]) {

    QCoreApplication app(argc, argv);
    QTextCodec::setCodecForCStrings(QTextCodec::codecForName(DEFAULT_STRING_CODEC));
    QStringList args = app.arguments();
    QRegExp rxEnableDebug("-d");
    QRegExp rxEnableTrace("-t");
    uint uid = getuid();

    bool debug = false, trace = false;
    for (int i = 1; i < args.size(); ++i) {
        if (rxEnableDebug.indexIn(args.at(i)) != -1 ) {
            debug = true;
        }
        if (rxEnableTrace.indexIn(args.at(i)) != -1 ) {
            debug = true;
            trace = true;
        }
    }

    /* Logger setup */
    ConsoleAppender *consoleAppender = new ConsoleAppender();
    Logger::registerAppender(consoleAppender);
    consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] <%c:(%F:%i)> %m\n");
    if (trace && debug)
        consoleAppender->setDetailsLevel(Logger::Trace);
    else if (debug && !trace)
        consoleAppender->setDetailsLevel(Logger::Debug);
    else {
        consoleAppender->setDetailsLevel(Logger::Info);
        consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] %m\n");
    }

    /* file lock setup */
    QString lockName = getHomeDir() + "/." + QString::number(uid) + ".pid";
    if (QFile::exists(lockName)) {
        bool ok;
        QString aPid = QString(readFileContents(lockName).c_str()).trimmed();
        uint pid = aPid.toInt(&ok, 10);
        if (ok) {
            if (pidIsAlive(pid)) {
                logError() << "Service Spawner is already running.";
                exit(LOCK_FILE_OCCUPIED_ERROR); /* can not open */
            } else
                logDebug() << "No alive Service Spawner pid found";

        } else {
            logWarn() << "Pid file is damaged or doesn't contains valid pid. File will be removed";
            QFile::remove(lockName);
        }
    }
    logDebug() << "Lock name:" << lockName;
    writeToFile(lockName, QString::number(getpid()), false); /* get process pid and record it to pid file no logrotate */

    if (uid == 0) {
        logInfo("Root Mode Service Spawner v" + QString(APP_VERSION) + ". " + QString(COPYRIGHT));
        setPublicDirPriviledges(getOrCreateDir(DEFAULT_PUBLIC_DIR));
        // TODO: auto coreginxWatcher = …
        spawnSSForEachUser();

        /* Setting up root watchers */
        new SvdUserWatcher(uid);

    } else {
        logInfo("Service Spawner v" + QString(APP_VERSION) + ". " + QString(COPYRIGHT));
        logDebug() << "Spawning for uid:" << getuid();

        logDebug() << "Checking user directory priviledges";
        setUserDirPriviledges(getHomeDir());

        /* Setting up user watchers */
        new SvdUserWatcher();
    }

    signal(SIGINT, unixSignalHandler);

    new LoggerTimer(consoleAppender);

    return app.exec();
}