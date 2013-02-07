/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#include "config_loader.h"



/*
 *  Read file contents of text file
 */
QString readFileContents(const QString& fileName) {
    QString lines = "";
    string line;
    ifstream file(fileName.toStdString().c_str(), ios::in);
    if (file.is_open()) {
        while ( file.good() ) {
            getline(file, line);
            lines += QString(line.c_str());
            // cout << line << endl;
        }
        file.close();
    } else {
        cerr << "Error reading file:" << fileName.toStdString() << endl;
        exit(NO_SUCH_FILE_ERROR);
    }
    return lines.trimmed();
}


/*
 *  Parse string contents to Json value.
 */
Json::Value parse(const QString& filename) {
    Json::Reader reader; /* parse json file */
    Json::Value root;
    bool parsedSuccess = reader.parse(readFileContents(filename).toStdString(), root, false);
    if (!parsedSuccess) {
        cerr << "JSON Parse Failure of file: " << filename.toStdString() << endl;
        exit(JSON_PARSE_ERROR);
    }
    return root; /* return user side igniter first by default */
}


/*
 *  Load igniter data in Json.
 */
Json::Value loadIgniter(const QString& name, uint uid) {
    const QString rootIgniter = QString(DEFAULTSOFTWARETEMPLATESDIR) + name + QString(DEFAULTSOFTWARETEMPLATEEXT);
    const QString userIgniter = QString(USERS_HOME_DIR) + QString::number(uid) + "/" + QString(DEFAULTUSERIGNITERSDIR) + name + QString(DEFAULTSOFTWARETEMPLATEEXT);

    QFile fileUser(userIgniter); /* try loading user igniter as first */
    QFile fileRoot(rootIgniter); /* try loading root igniter as second */
    if(!fileUser.open(QIODevice::ReadOnly)) { /* check file access */
        cerr << "No file: " << userIgniter.toStdString() << endl;
    } else
        return parse(userIgniter);

    if(!fileRoot.open(QIODevice::ReadOnly)) {
        cerr << "No file: " << rootIgniter.toStdString() << endl;
        exit(NO_SUCH_FILE_ERROR);
    } else
        return parse(rootIgniter);
}


/*
 * Service config loader.
 * Uses igniters to read service data from it and returns prepared object of Service
 */
const QString serviceConfigLoad(const QString& name, uint uid) {
    const QString defaultTemplateFile = QString(DEFAULTSOFTWARETEMPLATE) + QString(DEFAULTSOFTWARETEMPLATEEXT);
    Json::Value defaultTemplate = loadIgniter(name, uid);
    Json::Value appSpecificTemplate = loadIgniter(name, uid);

    const Json::Value installDefault = defaultTemplate["install"];
    const Json::Value installTemplateSpecific = appSpecificTemplate["install"];

    return QString(appSpecificTemplate.get("install", "No value").toStyledString().c_str());
}

