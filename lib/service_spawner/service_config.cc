/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#include "service_config.h"


SvdSchedulerAction::SvdSchedulerAction(const QString& initialCronEntry, const QString& initialCommands) {
    cronEntry = initialCronEntry;
    commands = initialCommands;
}


SvdServiceConfig::SvdServiceConfig() { /* Load default values */
    uid = getuid();
    schedulerActions = new QList<SvdSchedulerAction*>();
    try {
        auto defaults = (new SvdConfigLoader())->config;
        name = "Default";
        softwareName = (*defaults)["softwareName"].asString().c_str();
        autoRestart = (*defaults)["autoRestart"].asBool();
        autoStart = (*defaults)["autoStart"].asBool();
        reportAllErrors = (*defaults)["reportAllErrors"].asBool();
        reportAllInfos = (*defaults)["reportAllInfos"].asBool();
        reportAllDebugs = (*defaults)["reportAllDebugs"].asBool();
        watchPort = (*defaults)["watchPort"].asBool();
        staticPort = (*defaults)["staticPort"].asInt();

        /* laod service hooks */
        install = new SvdShellOperations(
            (*defaults)["install"]["commands"].asString().c_str(),
            (*defaults)["install"]["expectOutput"].asString().c_str());

        configure = new SvdShellOperations(
            (*defaults)["configure"]["commands"].asString().c_str(),
            (*defaults)["configure"]["expectOutput"].asString().c_str());

        start = new SvdShellOperations(
            (*defaults)["start"]["commands"].asString().c_str(),
            (*defaults)["start"]["expectOutput"].asString().c_str());

        afterStart = new SvdShellOperations(
            (*defaults)["afterStart"]["commands"].asString().c_str(),
            (*defaults)["afterStart"]["expectOutput"].asString().c_str());

        stop = new SvdShellOperations(
            (*defaults)["stop"]["commands"].asString().c_str(),
            (*defaults)["stop"]["expectOutput"].asString().c_str());

        afterStop = new SvdShellOperations(
            (*defaults)["afterStop"]["commands"].asString().c_str(),
            (*defaults)["afterStop"]["expectOutput"].asString().c_str());

        reload = new SvdShellOperations(
            (*defaults)["reload"]["commands"].asString().c_str(),
            (*defaults)["reload"]["expectOutput"].asString().c_str());

        validate = new SvdShellOperations(
            (*defaults)["validate"]["commands"].asString().c_str(),
            (*defaults)["validate"]["expectOutput"].asString().c_str());

        delete defaults;

    } catch (std::exception &e) {
        logDebug() << "Thrown Exception: " << e.what() << " in Default service.";
        exit(JSON_FORMAT_EXCEPTION_ERROR);
    } catch (...) {
        logDebug() << "Exception !";
        exit(OTHER_EXCEPTION_ERROR);
    }
}


/* destructor with memory free - welcome in C++ dmilith */
SvdServiceConfig::~SvdServiceConfig() {

    /* laod service hooks */
    delete install;
    delete configure;
    delete start;
    delete afterStart;
    delete stop;
    delete afterStop;
    delete reload;
    delete validate;
    for (int i = 0; i < schedulerActions->size(); i++)
        delete schedulerActions->at(i);
    delete schedulerActions;

}


SvdServiceConfig::SvdServiceConfig(const QString& serviceName) {
    schedulerActions = new QList<SvdSchedulerAction*>();
    uid = getuid();
    try {
        auto defaults = (new SvdConfigLoader())->config;
        auto root = (new SvdConfigLoader(serviceName))->config; // NOTE: the question is.. how will this behave ;]

        name = serviceName;
        softwareName = root->get("softwareName", (*defaults)["softwareName"]).asString().c_str();
        autoRestart = root->get("autoRestart", (*defaults)["autoRestart"]).asBool();
        autoStart = root->get("autoStart", (*defaults)["autoStart"]).asBool();
        reportAllErrors = root->get("reportAllErrors", (*defaults)["reportAllErrors"]).asBool();
        reportAllInfos = root->get("reportAllInfos", (*defaults)["reportAllInfos"]).asBool();
        reportAllDebugs = root->get("reportAllDebugs", (*defaults)["reportAllDebugs"]).asBool();
        watchPort = root->get("watchPort", (*defaults)["watchPort"]).asBool();
        staticPort = root->get("staticPort", (*defaults)["staticPort"]).asInt();

        /* load service scheduler data */
        for (uint index = 0; index < (*root)["schedulerActions"].size(); ++index ) {
            try {
                schedulerActions->push_back(
                    new SvdSchedulerAction(
                        (*root)["schedulerActions"][index].get("cronEntry", "0 0/10 * * * ?").asString().c_str(),
                        (*root)["schedulerActions"][index].get("shellCommands", "true").asString().c_str()
                    ));
            } catch (std::exception &e) {
                logDebug() << "Exception while parsing scheduler actions of" << name;
            }
            logDebug() << "Defined scheduler action";
        }

        /* laod service hooks */
        install = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["install"].get("commands", (*defaults)["install"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["install"].get("expectOutput", (*defaults)["install"]["expectOutput"]).asString().c_str()));

        configure = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["configure"].get("commands", (*defaults)["configure"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["configure"].get("expectOutput", (*defaults)["configure"]["expectOutput"]).asString().c_str()));

        start = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["start"].get("commands", (*defaults)["start"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["start"].get("expectOutput", (*defaults)["start"]["expectOutput"]).asString().c_str()));

        afterStart = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["afterStart"].get("commands", (*defaults)["afterStart"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["afterStart"].get("expectOutput", (*defaults)["afterStart"]["expectOutput"]).asString().c_str()));

        stop = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["stop"].get("commands", (*defaults)["stop"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["stop"].get("expectOutput", (*defaults)["stop"]["expectOutput"]).asString().c_str()));

        afterStop = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["afterStop"].get("commands", (*defaults)["afterStop"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["afterStop"].get("expectOutput", (*defaults)["afterStop"]["expectOutput"]).asString().c_str()));

        reload = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["reload"].get("commands", (*defaults)["reload"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["reload"].get("expectOutput", (*defaults)["reload"]["expectOutput"]).asString().c_str()));

        validate = new SvdShellOperations(
            replaceAllSpecialsIn((*root)["validate"].get("commands", (*defaults)["validate"]["commands"]).asString().c_str()),
            replaceAllSpecialsIn((*root)["validate"].get("expectOutput", (*defaults)["validate"]["expectOutput"]).asString().c_str()));

        delete defaults;
        delete root;

    } catch (std::exception &e) {
        logError() << "Thrown Exception: " << e.what() << " in " << serviceName << " service.";
        // exit(JSON_FORMAT_EXCEPTION_ERROR);
    } catch (...) {
        logError() << "Exception !";
        // exit(OTHER_EXCEPTION_ERROR);
    }
}


QString SvdServiceConfig::replaceAllSpecialsIn(const QString& content) {
    QString ccont = content;
    QString userServiceRoot = QString(USERS_HOME_DIR) + "/" + QString::number(uid) + "/Apps/" + softwareName;
    QString serviceRoot = QString(SOFTWARE_DIR) + "/" + softwareName; // low prio

    if (name == QString("Default")) {
        logDebug() << "No specials in Default file.";
        return ccont;
    } else {

        /* Replace SERVICE_ROOT */
        if (QFile::exists(userServiceRoot)) {
            logDebug() << "User service root found in: " << userServiceRoot;
            ccont = ccont.replace("SERVICE_ROOT", userServiceRoot);
        } else {
            logDebug() << "Not found user service root of " << name << userServiceRoot;
        }

        if (QFile::exists(serviceRoot)) {
            logDebug() << "Service root found in: " << serviceRoot;
            ccont = ccont.replace("SERVICE_ROOT", serviceRoot);
        } else {
             logWarn() << "Not found root service of " << name << serviceRoot;
             return "";
        }

        /* Replace SERVICE_PREFIX */
        QString prefixDir = QString(USERS_HOME_DIR) + "/" + QString::number(uid) + QString(SOFTWARE_DATA_DIR) + "/" + name;
        ccont = ccont.replace("SERVICE_PREFIX", prefixDir);

        /* Replace SERVICE_DOMAIN */
        QString domain = QString(DEFAULT_SYSTEM_DOMAIN);
        QString domainFilePath = prefixDir + "/" + QString(DEFAULT_USER_DOMAIN_FILE);
        QString userDomain = "";
        if (QFile::exists(domainFilePath)) {
            userDomain = QString(readFileContents(domainFilePath).c_str()).trimmed();
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
                auto address = info.addresses().first();
                userAddress = address.toString();
                // logDebug() << "Resolved address of domain " << userDomain << " is " << userAddress;
                ccont = ccont.replace("SERVICE_ADDRESS", userAddress); /* replace with user address content */
            } else {
                logDebug() << "Empty domain resolve of: " << userDomain;
                ccont = ccont.replace("SERVICE_ADDRESS", address); /* replace with user address content */
            }
        } else {
            // logDebug() << "Filling address with default value";
            ccont = ccont.replace("SERVICE_ADDRESS", address);
        }

        /* Replace SERVICE_PORT */
        QString portFilePath = prefixDir + "/" + QString(DEFAULT_USER_PORTS_FILE);
        if (QFile::exists(portFilePath)) {
            portFilePath = QString(readFileContents(portFilePath).c_str()).trimmed();
            ccont = ccont.replace("SERVICE_PORT", portFilePath); /* replace with user port content */
        } else {
            logDebug() << "No port file for service " << name << " (software: " << softwareName << ")! This might be something nasty!. It happened in file: " << portFilePath;
            ccont = ccont.replace("SERVICE_PORT", QString::number(registerFreeTcpPort())); /* this shouldn't happen */
        }

        // logDebug() << "Given content: " << ccont.replace("\n", " ");
        return ccont;
    }
}


SvdShellOperations::SvdShellOperations() {
    commands = "";
    expectOutput = "";
}


SvdShellOperations::SvdShellOperations(const QString& initialCommand, const QString& initialExpectOutput) {
    commands += initialCommand;
    expectOutput += initialExpectOutput;
}
