/**
 *  @author tallica, dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __UTILS_H__
#define __UTILS_H__

#include "../globals/globals.h"
#include "../jsoncpp/json/json.h"
#include "../cutelogger/Logger.h"
#include "../cutelogger/ConsoleAppender.h"
#include "../cutelogger/FileAppender.h"

#define logTrace LOG_TRACE
#define logDebug LOG_DEBUG
#define logInfo  LOG_INFO
#define logWarn  LOG_WARNING
#define logError LOG_ERROR
#define logFatal LOG_FATAL

#include <QtCore>
#include <QTime>
#include <QTextCodec>
#include <QtNetwork/QHostInfo>
#include <QtNetwork/QTcpServer>
#include <QtNetwork/QNetworkInterface>


using namespace std;


QString toHMS(uint duration);
QString getOrCreateDir(const QString& path);
void touch(const QString& fileName);
void writeToFile(const QString& fileName, const QString& contents);
uint registerFreeTcpPort(uint specificPort = 0);
Json::Value* parseJSON(const QString& filename);
string readFileContents(const QString& fileName);
bool removeDir(const QString& dirName);

const QString getHomeDir();
const QString getSoftwareDataDir();
const QString getServiceDataDir(const QString& name);
const QString getHomeDir(uid_t uid);
const QString getSoftwareDataDir(uid_t uid);
const QString getServiceDataDir(uid_t uid, const QString& name);

#endif
