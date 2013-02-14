/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "webapp_types.h"


bool shouldExist(const QStringList filesThatShouldExist, const QString& path) {
    Q_FOREACH(QString file, filesThatShouldExist) {
        logDebug() << "Checking existance of file:" << path + file;
        if (not QFile::exists(path + file)) return false;
    }
    return true;
}


bool shouldNotExist(const QStringList filesThatShouldNotExist, const QString& path) {
    Q_FOREACH(QString file, filesThatShouldNotExist) {
        logDebug() << "Checking nonexistance of file:" << path + file;
        if (QFile::exists(path + file)) return false;
    }
    return true;
}


WebAppTypeDetector::WebAppTypeDetector(const QString& path) {

    QStringList filesThatShouldExist, filesThatShouldNotExist;
    for (WebAppTypes i = StaticSite; i <= NoType; i++) {
        this->type = "NoType";
        filesThatShouldExist = QStringList();
        filesThatShouldNotExist = QStringList();

        switch (i) {
            case StaticSite:
                this->type = "Static";
                filesThatShouldExist << "/index.html";
                filesThatShouldNotExist << "/package.json" << "/Gemfile";
                break;

            case RailsSite:
                this->type = "Rails";
                filesThatShouldExist << "/config/boot.rb" << "/Gemfile" << "/Rakefile";
                filesThatShouldNotExist << "/index.html";
                break;

            case NodeSite:
                this->type = "Node";
                filesThatShouldExist << "/package.json";
                filesThatShouldNotExist << "/index.html";
                break;

            default:
                this->type = "NoType";
                break;
        }

        if (shouldExist(filesThatShouldExist, path) and shouldNotExist(filesThatShouldNotExist, path)) {
            logDebug() << "Passed all requirements of type:" << type;
            this->appType = i;
            return;
        }

    }
}


WebAppTypes WebAppTypeDetector::getType() {
    return this->appType;
}
