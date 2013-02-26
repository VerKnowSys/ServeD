/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_CONFIG__
#define __SERVICE_CONFIG__

#include <QtCore>
#include "utils.h"


class LoggerTimer: QObject {
    Q_OBJECT

    private:
        ConsoleAppender *logger;

    public:
        LoggerTimer(ConsoleAppender *appender);

    public slots:
        void invokeTrigger();

};

#endif
