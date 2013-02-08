/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_CONFIG__
#define __SERVICE_CONFIG__

#include "../globals/globals.h"
#include "utils.h"
#include "config_loader.h"

#include <QObject>
#include <QFile>
#include <QTime>
#include <QtNetwork/QHostInfo>
#include <QtNetwork/QTcpServer>
#include <QtNetwork/QNetworkInterface>


class SvdSchedulerAction {

    public:
        SvdSchedulerAction(const QString& initialCronEntry, const QString& initialCommands);
        QString cronEntry, commands;

};


class SvdShellOperations {
    public:
        SvdShellOperations();
        SvdShellOperations(const QString& initialCommand, const QString& initialExpectOutput);

        QString commands;
        QString expectOutput;

};


class SvdServiceConfig : QObject {
    Q_OBJECT

    public:
        SvdServiceConfig(); /* Load default values */
        SvdServiceConfig(const QString& serviceName);
        ~SvdServiceConfig(); /* free allocated objects */

        QString replaceAllSpecialsIn(const QString& content);

        uint uid; // user uid who loads igniter config

        QString name, softwareName;
        bool autoRestart, autoStart, reportAllErrors, reportAllInfos, reportAllDebugs, watchPort;
        int staticPort;
        QList<SvdSchedulerAction*> *schedulerActions;
        SvdShellOperations *install, *configure, *start, *afterStart, *stop, *afterStop, *reload, *validate;

};



#endif

