#include "logger.h"


void LoggerTimer::invokeTrigger() {
    QString dir = getHomeDir();
    if (QFile::exists(dir + "/.info")) {
        logInfo() << "Invoked logger level change to level 'info'.";
        QFile::remove(dir + "/.info");
        logger->setDetailsLevel(Logger::Info);
    }

    if (QFile::exists(dir + "/.debug")) {
        logInfo() << "Invoked logger level change to level 'debug'.";
        QFile::remove(dir + "/.debug");
        logger->setDetailsLevel(Logger::Debug);
    }

    if (QFile::exists(dir + "/.trace")) {
        logInfo() << "Invoked logger level change to level 'trace'.";
        QFile::remove(dir + "/.trace");
        logger->setDetailsLevel(Logger::Trace);
    }
}


LoggerTimer::LoggerTimer(ConsoleAppender *appender) {
    this->logger = appender;
    QTimer *timer = new QTimer(this);
    timer->setInterval(1000); // XXX : hardcoded
    connect(timer, SIGNAL(timeout()), this, SLOT(invokeTrigger()));
    timer->start();
}
