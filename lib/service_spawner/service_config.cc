/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#include "service_config.h"
#include "config_loader.h"


extern QString readFileContents(const QString& fileName);


uint registerFreeTcpPort(uint specificPort) {
    QTime midnight(0, 0, 0);
    // val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
    uint port = 0;
    int rand = (qrand() % 50000);
    if (specificPort == 0) {
        qsrand(midnight.secsTo(QTime::currentTime()));
        port = 10000 + rand;
    } else
        port = specificPort;

    #ifdef DEBUG
        cerr << "Trying port: " << port << ". Randseed: " << rand << endl;
    #endif

    QNetworkInterface *inter = new QNetworkInterface();
    QList<QHostAddress> list = inter->allAddresses(); /* all interfaces */
    #ifdef DEBUG
        cerr << "Addresses amount: " << list.size() << endl;
    #endif
    for (int j = 0; j < list.size(); j++) {
        QString hostName = list.at(j).toString();
        // cerr << "Trying hostname: " << hostName.toStdString() << endl;
        QHostInfo info = QHostInfo::fromName(hostName);
        if (!info.addresses().isEmpty()) {
            QHostAddress address = info.addresses().first();
            #ifdef DEBUG
                cerr << "Current address: " << address.toString().toStdString() << endl;
            #endif
            QTcpServer *tcpServer = new QTcpServer();
            if (!tcpServer->listen(address, port)) {
                #ifdef DEBUG
                    cerr << "Already taken port found: " << port << endl;
                #endif
                return registerFreeTcpPort(10000 + rand);
            } else {
                tcpServer->close();
            }
        } else {
            cerr << "No network interfaces available. Skipping" << endl;
        }
    }
    return port;
}


SvdSchedulerAction::SvdSchedulerAction(const QString& initialCronEntry, const QString& initialCommands) {
    cronEntry = initialCronEntry;
    commands = initialCommands;
}


SvdServiceConfig::SvdServiceConfig() { /* Load default values */
    uid = getuid();
    try {
        Json::Value defaults = (new SvdConfigLoader())->config;
        name = "Default";
        softwareName = defaults["softwareName"].asString().c_str();
        autoRestart = defaults["autoRestart"].asBool();
        autoStart = defaults["autoStart"].asBool();
        reportAllErrors = defaults["reportAllErrors"].asBool();
        reportAllInfos = defaults["reportAllInfos"].asBool();
        reportAllDebugs = defaults["reportAllDebugs"].asBool();
        watchPort = defaults["watchPort"].asBool();
        staticPort = defaults["staticPort"].asInt();

        /* load service scheduler data */
        Json::Value _preSchedActions = defaults["schedulerActions"];
        for ( uint index = 0; index < _preSchedActions.size(); ++index ) {
            schedulerActions.push_back(
                new SvdSchedulerAction(
                    _preSchedActions[index].get("cronEntry", "0 0/10 * * * ?").asString().c_str(),
                    _preSchedActions[index].get("shellCommands", "true").toStyledString().c_str() // HACK
                ));
        }

        /* laod service hooks */
        install = new SvdShellOperations(
            defaults["install"]["commands"].asString().c_str(),
            defaults["install"]["expectOutput"].asString().c_str());

        configure = new SvdShellOperations(
            defaults["configure"]["commands"].asString().c_str(),
            defaults["configure"]["expectOutput"].asString().c_str());

        start = new SvdShellOperations(
            defaults["start"]["commands"].asString().c_str(),
            defaults["start"]["expectOutput"].asString().c_str());

        afterStart = new SvdShellOperations(
            defaults["afterStart"]["commands"].asString().c_str(),
            defaults["afterStart"]["expectOutput"].asString().c_str());

        stop = new SvdShellOperations(
            defaults["stop"]["commands"].asString().c_str(),
            defaults["stop"]["expectOutput"].asString().c_str());

        afterStop = new SvdShellOperations(
            defaults["afterStop"]["commands"].asString().c_str(),
            defaults["afterStop"]["expectOutput"].asString().c_str());

        reload = new SvdShellOperations(
            defaults["reload"]["commands"].asString().c_str(),
            defaults["reload"]["expectOutput"].asString().c_str());

        validate = new SvdShellOperations(
            defaults["validate"]["commands"].asString().c_str(),
            defaults["validate"]["expectOutput"].asString().c_str());

    } catch (std::exception &e) {
        cerr << "Thrown Exception: " << e.what() << " in Default service." << endl;
        exit(JSON_FORMAT_EXCEPTION_ERROR);
    } catch (...) {
        cerr << "Exception !" << endl;
        exit(OTHER_EXCEPTION_ERROR);
    }
}


SvdServiceConfig::SvdServiceConfig(const QString& serviceName) {
    uid = getuid();
    try {
        Json::Value defaults = (new SvdConfigLoader())->config;
        Json::Value root = (new SvdConfigLoader(serviceName))->config; // NOTE: the question is.. how will this behave ;]

        name = serviceName;
        softwareName = root.get("softwareName", defaults["softwareName"]).asString().c_str();
        autoRestart = root.get("autoRestart", defaults["autoRestart"]).asBool();
        autoStart = root.get("autoStart", defaults["autoStart"]).asBool();
        reportAllErrors = root.get("reportAllErrors", defaults["reportAllErrors"]).asBool();
        reportAllInfos = root.get("reportAllInfos", defaults["reportAllInfos"]).asBool();
        reportAllDebugs = root.get("reportAllDebugs", defaults["reportAllDebugs"]).asBool();
        watchPort = root.get("watchPort", defaults["watchPort"]).asBool();
        staticPort = root.get("staticPort", defaults["staticPort"]).asInt();

        /* load service scheduler data */
        Json::Value _preSchedActions = root["schedulerActions"];
        for ( uint index = 0; index < _preSchedActions.size(); ++index ) {
            schedulerActions.push_back(
                new SvdSchedulerAction(
                    _preSchedActions[index].get("cronEntry", "0 0/10 * * * ?").asString().c_str(),
                    _preSchedActions[index].get("shellCommands", "true").toStyledString().c_str() // HACK
                ));
        }

        /* laod service hooks */
        install = new SvdShellOperations(
            replaceAllSpecialsIn(root["install"].get("commands", defaults["install"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["install"].get("expectOutput", defaults["install"]["expectOutput"]).asString().c_str()));

        configure = new SvdShellOperations(
            replaceAllSpecialsIn(root["configure"].get("commands", defaults["configure"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["configure"].get("expectOutput", defaults["configure"]["expectOutput"]).asString().c_str()));

        start = new SvdShellOperations(
            replaceAllSpecialsIn(root["start"].get("commands", defaults["start"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["start"].get("expectOutput", defaults["start"]["expectOutput"]).asString().c_str()));

        afterStart = new SvdShellOperations(
            replaceAllSpecialsIn(root["afterStart"].get("commands", defaults["afterStart"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["afterStart"].get("expectOutput", defaults["afterStart"]["expectOutput"]).asString().c_str()));

        stop = new SvdShellOperations(
            replaceAllSpecialsIn(root["stop"].get("commands", defaults["stop"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["stop"].get("expectOutput", defaults["stop"]["expectOutput"]).asString().c_str()));

        afterStop = new SvdShellOperations(
            replaceAllSpecialsIn(root["afterStop"].get("commands", defaults["afterStop"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["afterStop"].get("expectOutput", defaults["afterStop"]["expectOutput"]).asString().c_str()));

        reload = new SvdShellOperations(
            replaceAllSpecialsIn(root["reload"].get("commands", defaults["reload"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["reload"].get("expectOutput", defaults["reload"]["expectOutput"]).asString().c_str()));

        validate = new SvdShellOperations(
            replaceAllSpecialsIn(root["validate"].get("commands", defaults["validate"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn(root["validate"].get("expectOutput", defaults["validate"]["expectOutput"]).asString().c_str()));


    } catch (std::exception &e) {
        cerr << "Thrown Exception: " << e.what() << " in " << serviceName.toStdString() << " service." << endl;
        exit(JSON_FORMAT_EXCEPTION_ERROR);
    } catch (...) {
        cerr << "Exception !" << endl;
        exit(OTHER_EXCEPTION_ERROR);
    }
}


QString SvdServiceConfig::replaceAllSpecialsIn(const QString& content) {
    QString ccont = content;
    QString userServiceRoot = QString(USERS_HOME_DIR) + QString::number(uid) + "/Apps/" + softwareName + "/";
    QString serviceRoot = QString(SOFTWARE_DIR) + softwareName + "/"; // low prio

    if (name == QString("Default")) {
        cout << "No specials in Default file." << endl;
        return ccont;
    } else {

        /* Replace SERVICE_ROOT */
        QFile userServiceRootFile(userServiceRoot);
        if (userServiceRootFile.exists()) {
            // cout << "User service root found in: " << userServiceRoot.toStdString() << endl;
            ccont = ccont.replace("SERVICE_ROOT", userServiceRoot);
        } else {
            // cout << "Not found user service root of " << name.toStdString() << " " << userServiceRoot.toStdString() << endl;
        }
        QFile serviceRootFile(serviceRoot);
        if ((serviceRootFile.exists())) {
            // cout << "Service root found in: " << serviceRoot.toStdString() << endl;
            ccont = ccont.replace("SERVICE_ROOT", serviceRoot);
        } else {
             cerr << "Not found root service of " << name.toStdString() << " " << serviceRoot.toStdString() << endl;
             return "";
             // exit(NO_SUCH_FILE_ERROR);
        }

        /* Replace SERVICE_PREFIX */
        QString prefixDir = QString(USERS_HOME_DIR) + QString::number(uid) + QString(SOFTWARE_DATA_DIR) + name;
        ccont = ccont.replace("SERVICE_PREFIX", prefixDir);

        /* Replace SERVICE_DOMAIN */
        QString domain = QString(DEFAULT_SYSTEM_DOMAIN);
        QString domainFilePath = prefixDir + "/" + QString(DEFAULT_USER_DOMAIN_FILE);
        QFile domainFile(domainFilePath);
        QString userDomain = "";
        if (domainFile.exists()) {
            userDomain = readFileContents(domainFilePath).trimmed();
            ccont = ccont.replace("SERVICE_DOMAIN", userDomain); /* replace with user domain content */
        } else
            ccont = ccont.replace("SERVICE_DOMAIN", domain); /* replace with default domain */

        /* Replace SERVICE_ADDRESS */
        QString address = QString(DEFAULT_SYSTEM_ADDRESS);
        QString userAddress = "";
        QHostInfo info;
        if (!userDomain.isEmpty()) {
            info = QHostInfo::fromName(QString(userDomain));
            if (!info.addresses().isEmpty()) {
                QHostAddress address = info.addresses().first();
                userAddress = address.toString();
                // cout << "Resolved address of domain " << userDomain.toStdString() << " is " << userAddress.toStdString() << endl;
                ccont = ccont.replace("SERVICE_ADDRESS", userAddress); /* replace with user address content */
            } else {
                cerr << "Empty domain resolve of: " << userDomain.toStdString() << endl;
                ccont = ccont.replace("SERVICE_ADDRESS", address); /* replace with user address content */
            }
        } else {
            // cerr << "Filling address with default value" << endl;
            ccont = ccont.replace("SERVICE_ADDRESS", address);
        }

        /* Replace SERVICE_PORT */
        QString portFilePath = prefixDir + "/" + QString(DEFAULT_USER_PORTS_FILE);
        QFile portFile(portFilePath);
        if (portFile.exists()) {
            portFilePath = readFileContents(portFilePath).trimmed();
            ccont = ccont.replace("SERVICE_PORT", portFilePath); /* replace with user port content */
        } else {
            cerr << "No port file for service " << name.toStdString() << " (software: " << softwareName.toStdString() << ")! This might be something nasty!. It happened in file: " << portFilePath.toStdString() << endl;
            ccont = ccont.replace("SERVICE_PORT", QString::number(registerFreeTcpPort())); /* this shouldn't happen */
        }

        cerr << "DEBUG: Given content: " << ccont.toStdString() << endl;
        return ccont;
    }
}


SvdShellOperations::SvdShellOperations() {
    commands = "";
    expectOutput = "";
}


SvdShellOperations::SvdShellOperations(const QString& initialCommand, const QString& initialExpectOutput) {
    SvdShellOperations();
    commands += initialCommand;
    expectOutput += initialExpectOutput;
}


// SvdShellOperations::SvdShellOperations(QList<QString> *initialCommands, QList<QString> *initialExpectOutput) {
//     commands = initialCommands;
//     expectOutput = initialExpectOutput;
// }
