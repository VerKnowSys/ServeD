/**
 *  @author tallica
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


const QString getHomeDir() {
    uid_t uid = getuid();
    if (uid == 0)
        return QString(SYSTEM_USERS_DIR);
    else
        return QString(USERS_HOME_DIR) + "/" + QString::number(uid);
}


const QString getSoftwareDataDir() {
    QString dataDir = getHomeDir() + QString(SOFTWARE_DATA_DIR);
    if (!QFile::exists(dataDir)) {
        logTrace() << "Software data dir:" << dataDir << ", doesn't exists. Creating it.";
        QDir().mkpath(dataDir);
    }
    return dataDir;
}


const QString getServiceDataDir(const QString& name) {
    return getSoftwareDataDir() + "/" + name;
}


/* author: dmilith */
uint registerFreeTcpPort(uint specificPort) {
    QTime midnight(0, 0, 0);
    uint port = 0, rand = (qrand() % 40000);
    qsrand(midnight.msecsTo(QTime::currentTime()));
    if (specificPort == 0) {
        port = 10000 + rand;
    } else
        port = specificPort;

    logTrace() << "Trying port: " << port << ". Randseed: " << rand;
    auto tcpServer = new QTcpServer();
    if (!tcpServer->listen(QHostAddress::Any, port) && (!tcpServer->isListening()) ) {
        logDebug() << "Already taken port found: " << port;
        delete tcpServer;
        return registerFreeTcpPort(10000 + rand);
    } else {
        tcpServer->close();
        delete tcpServer;
    }
    return port;
}


// XXX: this code might be used later to get all network interfaces available on current machine:
// auto inter = new QNetworkInterface();
// auto list = inter->allAddresses(); /* all interfaces */
// logDebug() << "Addresses amount: " << list.size();
// for (int j = 0; j < list.size(); j++) {
    // QString hostName = list.at(j).toString();
    // logDebug() << "Trying hostname: " << hostName;
    // QHostInfo info = QHostInfo::fromName(hostName);
    // if (!info.addresses().isEmpty()) {
        // auto address = info.addresses().first();
        // logDebug() << "Current address: " << address.toString();
        //     } else {
//         logTrace() << "No network interfaces available. Skipping";
//     }
// }
// delete inter;




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
        logDebug() << fileName << ":" << line;
        lines += line;
    }
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
