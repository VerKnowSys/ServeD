/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_CONFIG__
#define __SERVICE_CONFIG__

#include "../kickstart/core.h"
#include <QObject>
#include <QFile>
#include <QTextStream>


class SvdSchedulerAction {

    public:
        SvdSchedulerAction(const QString& initialCronEntry, const QString& initialCommands);
        QString cronEntry, commands;

};


class SvdShellOperations {
    public:
        SvdShellOperations();
        SvdShellOperations(const QString& initialCommand, const QString& initialExpectOutput);
        // SvdShellOperations(QList<QString> *initialCommands, QList<QString> *initialExpectOutput);

        QString commands;
        QString expectOutput;

};


class SvdServiceConfig : QObject {
    Q_OBJECT

    public:
        SvdServiceConfig(); /* Load default values */
        SvdServiceConfig(const QString& serviceName);

    // private:
        QString name, softwareName;
        bool autoRestart, autoStart, reportAllErrors, reportAllInfos, reportAllDebugs, watchPort;
        int staticPort;
        QList<SvdSchedulerAction*> schedulerActions;
        SvdShellOperations *install, *configure, *start, *afterStart, *stop, *afterStop, *reload, *validate;

};



#endif

