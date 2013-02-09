/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "../globals/globals.h"
#include "service_config.h"
#include "service_watcher.h"
#include "utils.h"

#include <QtCore>


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

    softwareDataDir = getSoftwareDataDir();

    logDebug() << "Looking for services inside" << softwareDataDir;
    services = QDir(softwareDataDir).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);

    Q_FOREACH(QString name, services)
        logDebug() << "Found" << name << "service.";


    QFuture<void> result = QtConcurrent::map(services, spawnSvdServiceWatcher);
    watcher.setFuture(result);

    return app.exec();
}