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
#include <QObject>
#include <QFile>
#include <QTextStream>

// class

const QString serviceConfigLoad(const QString& igniterFileName, uint uid);


#endif

