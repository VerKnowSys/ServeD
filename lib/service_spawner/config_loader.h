/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#ifndef __CONFIG_LOADER__
#define __CONFIG_LOADER__

#include "../kickstart/core.h"
#include "../jsoncpp/json/json.h"
#include "service_config.h"

#include <QObject>
#include <QFile>
#include <QTextStream>


Json::Value serviceDataLoad(const QString& name, uint uid);
Json::Value defaultIgniterDataLoad();

Json::Value loadIgniter(const QString& name, uint uid);
Json::Value parse(const QString& filename);
QString readFileContents(const QString& fileName);


#endif

