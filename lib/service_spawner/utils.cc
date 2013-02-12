/**
 *  @author tallica, dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "utils.h"


void touch(const QString& fileName) {
    QFile file(fileName);
    file.open(QIODevice::WriteOnly | QIODevice::Text);
    file.close();
    return;
}


bool removeDir(const QString& dirName) {
    bool result = false;
    QDir dir(dirName);
    if (dir.exists(dirName)) {
        Q_FOREACH(QFileInfo info, dir.entryInfoList(QDir::NoDotAndDotDot | QDir::System | QDir::Hidden  | QDir::AllDirs | QDir::Files, QDir::DirsFirst)) {
            if (info.isDir()) {
                result = removeDir(info.absoluteFilePath());
            } else {
                result = QFile::remove(info.absoluteFilePath());
            }
            if (!result) {
                return result;
            }
        }
        result = dir.rmdir(dirName);
    }
    return result;
}


void writeToFile(const QString& fileName, const QString& contents) {
    QFile file(fileName);
    if (file.open(QIODevice::ReadWrite)) {
        QTextStream stream(&file);
        stream << contents << endl;
    }
    file.close();
}


const QString getHomeDir(uid_t uid) {
    if (uid == 0)
        return QString(SYSTEM_USERS_DIR);
    else
        return QString(USERS_HOME_DIR) + "/" + QString::number(uid);
}


const QString getSoftwareDataDir(uid_t uid) {
    QString dataDir = getHomeDir(uid) + QString(SOFTWARE_DATA_DIR);
    if (!QFile::exists(dataDir)) {
        logTrace() << "Software data dir:" << dataDir << ", doesn't exists. Creating it.";
        QDir().mkpath(dataDir);
    }
    return dataDir;
}


const QString getServiceDataDir(uid_t uid, const QString& name) {
    return getSoftwareDataDir(uid) + "/" + name;
}


const QString getHomeDir() {
    return getHomeDir(getuid());
}


const QString getSoftwareDataDir() {
    return getSoftwareDataDir(getuid());
}


const QString getServiceDataDir(const QString& name) {
    return getServiceDataDir(getuid(), name);
}


/* author: dmilith */
uint registerFreeTcpPort(uint specificPort) {
    QTime midnight(0, 0, 0);
    qsrand(midnight.msecsTo(QTime::currentTime())); // accuracy is in ms.. so let's hack it a bit
    usleep(10000); // this practically means no chance to generate same port when generating multiple ports at once
    uint port = 0, rand = (qrand() % 40000);
    if (specificPort == 0) {
        port = 10000 + rand;
    } else
        port = specificPort;

    logTrace() << "Trying port: " << port << ". Randseed: " << rand;
    auto inter = new QNetworkInterface();
    auto list = inter->allAddresses(); /* all addresses on all interfaces */
    logDebug() << "Addresses amount: " << list.size();
    for (int j = 0; j < list.size(); j++) {
        QHostInfo info = QHostInfo::fromName(list.at(j).toString());
        if (!info.addresses().isEmpty()) {
            auto address = info.addresses().first();
            logDebug() << "Got address: " << address;
            auto tcpServer = new QTcpServer();
            tcpServer->listen(address, port);
            if (not tcpServer->isListening()) {
                logDebug() << "Taken port on address:" << address << ":" << port;
                delete tcpServer;
                delete inter;
                return registerFreeTcpPort(10000 + rand);
            } else
                tcpServer->close();
            delete tcpServer;
        }
    }
    delete inter;
    return port;
}


/*
 *  Read file contents of text file
 */
string readFileContents(const QString& fileName) {
    QString lines = "";
    QFile f(fileName);
    f.open(QIODevice::ReadOnly);
    QTextStream stream(&f);
    stream.setCodec(QTextCodec::codecForName(DEFAULT_STRING_CODEC));
    while (!stream.atEnd()) {
        QString line = stream.readLine();
        if (!line.trimmed().isEmpty()) {
            lines += line + "\n";
            logTrace() << fileName << ":" << line;
        }
    }
    lines += "\n";
    f.close();
    return string(lines.toUtf8());
}


/*
 *  Parse string contents to Json value.
 */
Json::Value* parseJSON(const QString& filename) {
    Json::Reader reader; /* parse json file */
    auto root = new Json::Value();
    auto parsedSuccess = reader.parse(readFileContents(filename), *root, false);
    if (!parsedSuccess) {
        logError() << "JSON Parse Failure of file: " << filename;
        return root;
    }
    return root; /* return user side igniter first by default */
}
