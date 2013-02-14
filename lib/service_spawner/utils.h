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

#include <sys/stat.h>
#include <errno.h>

#include <QtCore>
#include <QTime>
#include <QTextCodec>
#include <QtNetwork/QHostInfo>
#include <QtNetwork/QTcpServer>
#include <QtNetwork/QNetworkInterface>


using namespace std;


string readFileContents(const QString& fileName);

bool expect(const QString& inputFileContent, const QString& expectedString);
bool removeDir(const QString& dirName);
bool setPublicDirPriviledges(const QString& path);
bool setUserDirPriviledges(const QString& path);

void touch(const QString& fileName);
void writeToFile(const QString& fileName, const QString& contents);
void rotateLog(const QString& fileName);

uint registerFreeTcpPort(uint specificPort = 0);
Json::Value* parseJSON(const QString& filename);

const QString toHMS(uint duration);
const QString getOrCreateDir(const QString& path);
const QString getWebAppsDir();
const QString getHomeDir();
const QString getSoftwareDataDir();
const QString getServiceDataDir(const QString& name);
const QString getHomeDir(uid_t uid);
const QString getSoftwareDataDir(uid_t uid);
const QString getServiceDataDir(uid_t uid, const QString& name);

#endif
