/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "utils.h"


void setHomeDir(QString & homeDir) {
    if (getuid() == 0)
        homeDir = QString(SYSTEMUSERS_HOME_DIR);
    else
        homeDir = QString(USERS_HOME_DIR) + "/" + QString::number(getuid());
}


void setSoftwareDataDir(QString & softwareDataDir) {
    QString homeDir;
    setHomeDir(homeDir);
    softwareDataDir = homeDir + QString(SOFTWARE_DATA_DIR);
}


void setServiceDataDir(QString & serviceDataDir, const QString & name) {
    QString softwareDataDir;
    setSoftwareDataDir(softwareDataDir);
    serviceDataDir = softwareDataDir + "/" + name;
}


/* author: dmilith */
uint registerFreeTcpPort(uint specificPort) {
    QTime midnight(0, 0, 0);
    // val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
    uint port = 0;
    int rand = (qrand() % 50000);
    if (specificPort == 0) {
        qsrand(midnight.msecsTo(QTime::currentTime()));
        port = 10000 + rand;
    } else
        port = specificPort;

    #ifdef DEBUG
        logDebug() << "Trying port: " << port << ". Randseed: " << rand;
    #endif

    QNetworkInterface *inter = new QNetworkInterface();
    QList<QHostAddress> list = inter->allAddresses(); /* all interfaces */
    #ifdef DEBUG
        logDebug() << "Addresses amount: " << list.size();
    #endif
    for (int j = 0; j < list.size(); j++) {
        QString hostName = list.at(j).toString();
        // logDebug() << "Trying hostname: " << hostName;
        QHostInfo info = QHostInfo::fromName(hostName);
        if (!info.addresses().isEmpty()) {
            QHostAddress address = info.addresses().first();
            #ifdef DEBUG
                logDebug() << "Current address: " << address.toString();
            #endif
            QTcpServer *tcpServer = new QTcpServer();
            if (!tcpServer->listen(address, port)) {
                #ifdef DEBUG
                    logDebug() << "Already taken port found: " << port;
                #endif
                delete tcpServer;
                return registerFreeTcpPort(10000 + rand);
            } else {
                tcpServer->close();
                delete tcpServer;
            }
        } else {
            logDebug() << "No network interfaces available. Skipping";
        }
    }
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
    Json::Value* root = new Json::Value();
    bool parsedSuccess = reader.parse(readFileContents(filename), *root, false);
    if (!parsedSuccess) {
        logDebug() << "JSON Parse Failure of file: " << filename;
        return root;
    }
    return root; /* return user side igniter first by default */
}
