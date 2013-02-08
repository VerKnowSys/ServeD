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
        qDebug() << "Trying port: " << port << ". Randseed: " << rand << endl;
    #endif

    QNetworkInterface *inter = new QNetworkInterface();
    QList<QHostAddress> list = inter->allAddresses(); /* all interfaces */
    #ifdef DEBUG
        qDebug() << "Addresses amount: " << list.size() << endl;
    #endif
    for (int j = 0; j < list.size(); j++) {
        QString hostName = list.at(j).toString();
        // cerr << "Trying hostname: " << hostName.toStdString() << endl;
        QHostInfo info = QHostInfo::fromName(hostName);
        if (!info.addresses().isEmpty()) {
            QHostAddress address = info.addresses().first();
            #ifdef DEBUG
                qDebug() << "Current address: " << address.toString() << endl;
            #endif
            QTcpServer *tcpServer = new QTcpServer();
            if (!tcpServer->listen(address, port)) {
                #ifdef DEBUG
                    qDebug() << "Already taken port found: " << port << endl;
                #endif
                delete tcpServer;
                return registerFreeTcpPort(10000 + rand);
            } else {
                tcpServer->close();
                delete tcpServer;
            }
        } else {
            qDebug() << "No network interfaces available. Skipping" << endl;
        }
    }
    return port;
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
        qDebug() << "Error reading file:" << fileName << endl;
        exit(NO_SUCH_FILE_ERROR);
    }
    return lines;
}


/*
 *  Parse string contents to Json value.
 */
Json::Value* parseJSON(const QString& filename) {
    Json::Reader reader; /* parse json file */
    Json::Value* root = new Json::Value();
    bool parsedSuccess = reader.parse(readFileContents(filename).toStdString(), *root, false);
    if (!parsedSuccess) {
        cerr << "JSON Parse Failure of file: " << filename.toStdString() << endl;
        exit(JSON_PARSE_ERROR);
    }
    return root; /* return user side igniter first by default */
}
