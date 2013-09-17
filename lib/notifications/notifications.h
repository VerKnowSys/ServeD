/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */


#ifndef __NOTIFICATIONS_CENTER__
#define __NOTIFICATIONS_CENTER__


#include <QtCore>
#include <QtNetwork/QNetworkAccessManager>
#include <QtNetwork/QNetworkReply>
#include <QtNetwork/QNetworkRequest>
#include <sys/ioctl.h>

#include "../globals/globals.h"
#include "../cutelogger/Logger.h"
#include "../cutelogger/ConsoleAppender.h"
#include "../cutelogger/FileAppender.h"

#define NOTIFICATION_LEVEL_ERROR    0
#define NOTIFICATION_LEVEL_WARNING  1
#define NOTIFICATION_LEVEL_NOTICE   2

struct Notification {
    int level;
    QString content;
    QDateTime time;
};

enum NotificationLevels {NOTIFY, WARNING, ERROR, FATAL};


static QMap<QString, int> history = QMap<QString, int>(); /* content, amount */

/* XXX: helpers taken from TheSS utils.cc */
void writeToFile(const QString& fileName, const QString& contents);
QString readFileContents(const QString& fileName);
QString readFileContents(const QString& fileName);


void notification(const QString& notificationMessage, const QString& serviceName = "", NotificationLevels level = ERROR);


#endif
