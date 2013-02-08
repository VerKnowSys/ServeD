/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#include "config_loader.h"


/*
 * Default config loader.
 * Uses igniters to read service data from it and returns prepared object of Service
 */
SvdConfigLoader::SvdConfigLoader() {
    this->config = new Json::Value();
    this->name = QString("Default");
    this->uid = getuid();
    this->config = loadDefaultIgniter(); // load app specific igniter data
}


SvdConfigLoader::~SvdConfigLoader() {
    delete config;
}


/*
 * Service config loader.
 * Uses igniters to read service data from it and returns prepared object of Service
 */
SvdConfigLoader::SvdConfigLoader(QString preName) {
    this->config = new Json::Value();
    this->name = preName;
    this->uid = getuid();
    this->config = loadIgniter(); // load app specific igniter data
}


/*
 *  Load igniter data in Json.
 */
Json::Value* SvdConfigLoader::loadDefaultIgniter() {
    const QString defaultTemplateFile = QString(DEFAULTSOFTWARETEMPLATE) + QString(DEFAULTSOFTWARETEMPLATEEXT);
    Json::Value* result = new Json::Value();
    QFile defaultIgniter(defaultTemplateFile); /* try loading root igniter as second */
    if(!defaultIgniter.open(QIODevice::ReadOnly)) { /* check file access */
        #ifdef DEBUG
            cerr << "No file: " << defaultTemplateFile.toStdString() << endl;
        #endif
        // exit(NO_DEFAULT_IGNITERS_FOUND_ERROR);
    } else {
        result = parseJSON(defaultTemplateFile);
    }
    defaultIgniter.close();
    return result;
}


/*
 *  Load igniter data in Json.
 */
Json::Value* SvdConfigLoader::loadIgniter() {
    const QString rootIgniter = QString(DEFAULTSOFTWARETEMPLATESDIR) + "/" + name + QString(DEFAULTSOFTWARETEMPLATEEXT);
    const QString userIgniter = QString(USERS_HOME_DIR) + "/" + QString::number(uid) + QString(DEFAULTUSERIGNITERSDIR) + "/" + name + QString(DEFAULTSOFTWARETEMPLATEEXT);

    Json::Value* result = new Json::Value();
    QFile fileUser(userIgniter); /* try loading user igniter as first */
    QFile fileRoot(rootIgniter); /* try loading root igniter as second */
    if(!fileUser.open(QIODevice::ReadOnly)) { /* check file access */
        #ifdef DEBUG
            cerr << "No file: " << userIgniter.toStdString() << endl;
        #endif
    } else {
        fileRoot.close();
        fileUser.close();
        result = parseJSON(userIgniter);
        return result;
    }
    fileUser.close();

    if(!fileRoot.open(QIODevice::ReadOnly)) {
        #ifdef DEBUG
            cerr << "No file: " << rootIgniter.toStdString() << endl;
        #endif
        // exit(NO_SUCH_FILE_ERROR);
        fileRoot.close();
        result = new Json::Value();
        return result;
    }

    result = parseJSON(rootIgniter);
    fileRoot.close();
    return result;
}

