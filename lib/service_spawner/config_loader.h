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


class SvdConfigLoader : QObject {
    Q_OBJECT

    public:
        Json::Value config; // Igniter config
        QString name; // Igniter name
        uint uid; // user uid who loads igniter config
        SvdConfigLoader(); // this will load Default igniter
        SvdConfigLoader(const QString& preName); // this will set uid and name automatically

        // Json::Value serviceDataLoad(); // built into constructor
        Json::Value loadDefaultIgniter();
        Json::Value loadIgniter();

        Json::Value parseJSON(const QString& filename);
        QString readFileContents(const QString& fileName);
        QString replaceAllSpecialsIn(const QString& content);

};

#endif

