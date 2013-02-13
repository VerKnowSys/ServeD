/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "webapp_types.h"


bool WebAppType::detect(const QString& path) {
    return false; // default value. it's overloaded by child classes
}


bool StaticSiteType::detect(const QString& path) {
    filesThatShouldExist << "/index.html";
    filesThatShouldNotExist << "/Gemfile" << "/package.json";

    Q_FOREACH(QString file, filesThatShouldExist) {
        if (not QFile::exists(path + file)) return false;
    }
    Q_FOREACH(QString file, filesThatShouldNotExist) {
        if (QFile::exists(path + file)) return false;
    }

    logDebug() << "Static Site detected in path:" << path;
    return true;
}


bool RailsSiteType::detect(const QString& path) {
    filesThatShouldExist << "/Gemfile" << "/config/boot.rb" << "/public";
    filesThatShouldNotExist << "/index.html" << "/package.json";

    Q_FOREACH(QString file, filesThatShouldExist) {
        if (not QFile::exists(path + file)) return false;
    }
    Q_FOREACH(QString file, filesThatShouldNotExist) {
        if (QFile::exists(path + file)) return false;
    }
    logDebug() << "RailsSiteType detected in path:" << path;
    return true;
}


bool NodeSiteType::detect(const QString& path) {
    filesThatShouldExist << "/package.json";
    filesThatShouldNotExist << "/index.html" << "/Gemfile";

    Q_FOREACH(QString file, filesThatShouldExist) {
        if (not QFile::exists(path + file)) return false;
    }
    Q_FOREACH(QString file, filesThatShouldNotExist) {
        if (QFile::exists(path + file)) return false;
    }
    logDebug() << "NodeSiteType detected in path:" << path;
    return true;
}
