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
    QTextCodec::setCodecForCStrings(QTextCodec::codecForName(DEFAULT_STRING_CODEC));

    QStringList args = app.arguments();
    QRegExp rxEnableDebug("-d");
    QRegExp rxEnableTrace("-t");
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

    /* Setting up watchers */
    QFutureWatcher<void> watcher;
    QString softwareDataDir;
    QStringList services;

    /* Logger setup */
    ConsoleAppender *consoleAppender = new ConsoleAppender();
    consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] <%c> %m\n");
    Logger::registerAppender(consoleAppender);
    if (trace && debug)
        consoleAppender->setDetailsLevel(Logger::Trace);
    else if (debug && !trace)
        consoleAppender->setDetailsLevel(Logger::Debug);
    else
        consoleAppender->setDetailsLevel(Logger::Info);

    logInfo() << "Starting Service Spawner for uid:" << getuid();
    softwareDataDir = getSoftwareDataDir();

    logDebug() << "Looking for services inside" << softwareDataDir;
    services = QDir(softwareDataDir).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);

    Q_FOREACH(QString name, services)
        logInfo() << "Found" << name << "service.";


    QFuture<void> result = QtConcurrent::map(services, spawnSvdServiceWatcher);
    watcher.setFuture(result);

    return app.exec();
}