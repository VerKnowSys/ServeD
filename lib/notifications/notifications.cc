/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "../globals/globals.h"
#include "notifications.h"


/* XXX: NON DRY, functions copied from TheSS utils.cc: */
void rotateFile(const QString& fileName) {
    logWarn() << "NO-OP rotateFile" << fileName;
}


/* XXX: NON DRY, function copied from TheSS utils.cc: */
const QString getOrCreateDir(const QString& path) {
    if (not QFile::exists(path)) {
        logTrace() << "Creating non existant dir:" << path ;
        QDir().mkpath(path);
    }
    return path;
}


/* XXX: NON DRY, function copied from TheSS utils.cc: */
void writeToFile(const QString& fileName, const QString& contents, bool rotateThisFile) {
    if (rotateThisFile)
        rotateFile(fileName);
    QFile file(fileName);
    if (file.open(QIODevice::ReadWrite | QIODevice::Truncate | QIODevice::Text | QIODevice::Unbuffered)) {
        QTextStream stream(&file);
        stream << contents << endl;
    }
    file.close();
}


/* XXX: NON DRY, function copied from TheSS utils.cc: */
void writeToFile(const QString& fileName, const QString& contents) {
    writeToFile(fileName, contents, false);
}


/*
 *  Read file contents of text file
 */
QString readFileContents(const QString& fileName) {
    QString lines = "";
    QFile f(fileName);
    f.open(QIODevice::ReadOnly);
    QTextStream stream(&f);
    // stream.setCodec(QTextCodec::codecForName(DEFAULT_STRING_CODEC));
    while (!stream.atEnd()) {
        QString line = stream.readLine();
        if (!line.trimmed().isEmpty()) {
            lines += line + "\n";
            logTrace() << fileName << ":" << line;
        }
    }
    lines += "\n";
    f.close();
    return lines;
}


void moveOldNotificationsToHistoryAndCleanHistory(const QString& notificationRoot, const QString& historyRoot) {
    /* count notifications on "current" list */
    auto items = QDir(notificationRoot).entryList(QDir::Files, QDir::Time);
    int notificationAmount = items.size();
    logDebug() << "Notification items in dir:" << notificationRoot << ":" << QString::number(notificationAmount);

    /* moving old items to history */
    for (int i = notificationAmount - 1; i > 0; i--) {
        if (i > NOTIFICATIONS_LAST_SHOWN) {
            QString item = items.at(i);
            logDebug() << "Moving old notification to history:" << item;
            QFile::copy(notificationRoot + "/" + item, historyRoot + "/" + item);
            QFile::remove(notificationRoot + "/" + item);
        }
    }

    /* history max amount check */
    items = QDir(historyRoot).entryList(QDir::Files, QDir::Time);
    notificationAmount = items.size();
    logDebug() << "Notification history items in dir:" << historyRoot << ":" << QString::number(notificationAmount);
    for (int i = notificationAmount - 1; i > 0; i--) {
        if (i > NOTIFICATIONS_HISTORY_KEEP_UPTO) {
            QString item = items.at(i);
            logDebug() << "Removing old notification from notification history:" << item;
            QFile::remove(historyRoot + "/" + item);
        }
    }
}


void notification(const QString& notificationMessage, const QString& serviceName, NotificationLevels level) {

    QString message;

    /* make sure that every notification begins with proper data and time */
    const QDateTime now = QDateTime::currentDateTime();
    if (not notificationMessage.startsWith(now.toString("dd-hh:mm"))) { // XXX: hardcoded
        message = now.toString("dd-hh:mm:ss - ") + notificationMessage;
    } else
        message = notificationMessage;

    QString notificationRoot = QString(getenv("HOME")) + SOFTWARE_DATA_DIR;
    QString postfix;
    switch (level) {
        case NOTIFY:
            logInfo() << notificationMessage;
            postfix = ".notice";
            break;

        case WARNING:
            logWarn() << notificationMessage;
            postfix = ".warning";
            break;

        case ERROR:
            logError() << notificationMessage;
            postfix = ".error";
            break;

        case FATAL:
            logFatal() << notificationMessage;
            postfix = ".fatal";
            break;

    }

    if (history[message] > 0) {
        history[message]++;
    } else {

        if (serviceName.isEmpty()) {
            QString historyRoot = notificationRoot + NOTIFICATIONS_HISTORY_DATA_DIR;
            notificationRoot += NOTIFICATIONS_DATA_DIR;
            getOrCreateDir(notificationRoot); /* create it only for common notifications - unrelated to services*/
            getOrCreateDir(historyRoot);

            auto hash = new QCryptographicHash(QCryptographicHash::Sha1);
            QString content = message;
            hash->addData(content.toUtf8(), content.length());
            QString notificationFileName = hash->result().toHex() + postfix;
            delete hash;
            writeToFile(notificationRoot + "/" + notificationFileName, message);

            moveOldNotificationsToHistoryAndCleanHistory(notificationRoot, historyRoot);

        } else { /* it's service side notification! */
            notificationRoot += "/" + serviceName + NOTIFICATIONS_DATA_DIR;
            QString historyRoot = QString(getenv("HOME")) + SOFTWARE_DATA_DIR + "/" + serviceName + NOTIFICATIONS_HISTORY_DATA_DIR;
            getOrCreateDir(notificationRoot); /* create it only for common notifications - unrelated to services*/
            getOrCreateDir(historyRoot);

            auto hash = new QCryptographicHash(QCryptographicHash::Sha1);
            QString content = message;
            hash->addData(content.toUtf8(), content.length());
            QString notificationFileName = hash->result().toHex() + postfix;
            delete hash;
            writeToFile(notificationRoot + "/" + notificationFileName, message);

            moveOldNotificationsToHistoryAndCleanHistory(notificationRoot, historyRoot);
        }
    }
}
