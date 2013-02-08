/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __UTILS_H__
#define __UTILS_H__

#include "../globals/globals.h"
#include "../jsoncpp/json/json.h"

#include <iostream>
#include <fstream>

#include <QtCore>
#include <QTime>
#include <QTextCodec>
#include <QtNetwork/QHostInfo>
#include <QtNetwork/QTcpServer>
#include <QtNetwork/QNetworkInterface>


using namespace std;


uint registerFreeTcpPort(uint specificPort = 0);
Json::Value* parseJSON(const QString& filename);
string readFileContents(const QString& fileName);

void setHomeDir(QString & homeDir);
void setSoftwareDataDir(QString & softwareDataDir);
void setServiceDataDir(QString & serviceDataDir, const QString & name);

#endif
