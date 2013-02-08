/**
 *  @author dmilith
 *
 *   Software config loader for json igniters.
 *   © 2011-2013 - VerKnowSys
 *
 */

#include "service_config.h"
#include "config_loader.h"


SvdSchedulerAction::SvdSchedulerAction(const QString& initialCronEntry, const QString& initialCommands) {
    cronEntry = initialCronEntry;
    commands = initialCommands;
}


SvdServiceConfig::SvdServiceConfig() { /* Load default values */
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
            root["install"].get("commands", defaults["install"]["commands"]).asString().c_str(),
            root["install"].get("expectOutput", defaults["install"]["expectOutput"]).asString().c_str());

        configure = new SvdShellOperations(
            root["configure"].get("commands", defaults["configure"]["commands"]).asString().c_str(),
            root["configure"].get("expectOutput", defaults["configure"]["expectOutput"]).asString().c_str());

        start = new SvdShellOperations(
            root["start"].get("commands", defaults["start"]["commands"]).asString().c_str(),
            root["start"].get("expectOutput", defaults["start"]["expectOutput"]).asString().c_str());

        afterStart = new SvdShellOperations(
            root["afterStart"].get("commands", defaults["afterStart"]["commands"]).asString().c_str(),
            root["afterStart"].get("expectOutput", defaults["afterStart"]["expectOutput"]).asString().c_str());

        stop = new SvdShellOperations(
            root["stop"].get("commands", defaults["stop"]["commands"]).asString().c_str(),
            root["stop"].get("expectOutput", defaults["stop"]["expectOutput"]).asString().c_str());

        afterStop = new SvdShellOperations(
            root["afterStop"].get("commands", defaults["afterStop"]["commands"]).asString().c_str(),
            root["afterStop"].get("expectOutput", defaults["afterStop"]["expectOutput"]).asString().c_str());

        reload = new SvdShellOperations(
            root["reload"].get("commands", defaults["reload"]["commands"]).asString().c_str(),
            root["reload"].get("expectOutput", defaults["reload"]["expectOutput"]).asString().c_str());

        validate = new SvdShellOperations(
            root["validate"].get("commands", defaults["validate"]["commands"]).asString().c_str(),
            root["validate"].get("expectOutput", defaults["validate"]["expectOutput"]).asString().c_str());


    } catch (std::exception &e) {
        cerr << "Thrown Exception: " << e.what() << " in " << serviceName.toStdString() << " service." << endl;
        exit(JSON_FORMAT_EXCEPTION_ERROR);
    } catch (...) {
        cerr << "Exception !" << endl;
        exit(OTHER_EXCEPTION_ERROR);
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
