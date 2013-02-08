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
    this->name = QString("Default");
    this->uid = getuid();
    this->config = loadDefaultIgniter(); // load app specific igniter data
}


/*
 * Service config loader.
 * Uses igniters to read service data from it and returns prepared object of Service
 */
SvdConfigLoader::SvdConfigLoader(QString preName) {
    this->name = preName;
    this->uid = getuid();
    this->config = loadIgniter(); // load app specific igniter data
}


/*
 *  Read file contents of text file
 */
QString readFileContents(const QString& fileName) {
    QString lines = "", l = "";
    string line;
    ifstream file(fileName.toStdString().c_str(), ios::in);
    if (file.is_open()) {
        while ( file.good() ) {
            getline(file, line);
            lines += QString(line.c_str()) + "\n";
        }
        file.close();
    } else {
        cerr << "Error reading file:" << fileName.toStdString() << endl;
        exit(NO_SUCH_FILE_ERROR);
    }
    return lines;
}


/*
 *  Parse string contents to Json value.
 */
Json::Value SvdConfigLoader::parseJSON(const QString& filename) {
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
Json::Value SvdConfigLoader::loadDefaultIgniter() {
    const QString defaultTemplateFile = QString(DEFAULTSOFTWARETEMPLATE) + QString(DEFAULTSOFTWARETEMPLATEEXT);
    QFile defaultIgniter(defaultTemplateFile); /* try loading root igniter as second */
    if(!defaultIgniter.open(QIODevice::ReadOnly)) { /* check file access */
        cerr << "No file: " << defaultTemplateFile.toStdString() << endl;
        exit(NO_DEFAULT_IGNITERS_FOUND_ERROR);
    } else
        return parseJSON(defaultTemplateFile);
}


/*
 *  Load igniter data in Json.
 */
Json::Value SvdConfigLoader::loadIgniter() {
    const QString rootIgniter = QString(DEFAULTSOFTWARETEMPLATESDIR) + name + QString(DEFAULTSOFTWARETEMPLATEEXT);
    const QString userIgniter = QString(USERS_HOME_DIR) + QString::number(uid) + "/" + QString(DEFAULTUSERIGNITERSDIR) + name + QString(DEFAULTSOFTWARETEMPLATEEXT);

    QFile fileUser(userIgniter); /* try loading user igniter as first */
    QFile fileRoot(rootIgniter); /* try loading root igniter as second */
    if(!fileUser.open(QIODevice::ReadOnly)) { /* check file access */
        cerr << "No file: " << userIgniter.toStdString() << endl;
    } else
        return parseJSON(userIgniter);

    if(!fileRoot.open(QIODevice::ReadOnly)) {
        cerr << "No file: " << rootIgniter.toStdString() << endl;
        exit(NO_SUCH_FILE_ERROR);
    } else
        return parseJSON(rootIgniter);
}

