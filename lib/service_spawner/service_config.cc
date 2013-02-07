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


SvdServiceConfig::SvdServiceConfig(const QString& serviceName) {

    try {
        Json::Value defaults = defaultIgniterDataLoad();
        Json::Value root = serviceDataLoad(serviceName, getuid()); // NOTE: the question is.. how will this behave ;]

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
                    _preSchedActions[index].get("shellCommands", "true").toStyledString().c_str()
                ));
        }

        /* laod service hooks */
        install = new SvdShellOperations(
            root["install"].get("commands", defaults["install"]["commands"]).toStyledString().c_str(),
            root["install"].get("expectOutput", defaults["install"]["expectOutput"]).toStyledString().c_str());

        configure = new SvdShellOperations(
            root["configure"].get("commands", defaults["configure"]["commands"]).toStyledString().c_str(),
            root["configure"].get("expectOutput", defaults["configure"]["expectOutput"]).toStyledString().c_str());

        start = new SvdShellOperations(
            root["start"].get("commands", defaults["start"]["commands"]).toStyledString().c_str(),
            root["start"].get("expectOutput", defaults["start"]["expectOutput"]).toStyledString().c_str());

        afterStart = new SvdShellOperations(
            root["afterStart"].get("commands", defaults["afterStart"]["commands"]).toStyledString().c_str(),
            root["afterStart"].get("expectOutput", defaults["afterStart"]["expectOutput"]).toStyledString().c_str());

        stop = new SvdShellOperations(
            root["stop"].get("commands", defaults["stop"]["commands"]).toStyledString().c_str(),
            root["stop"].get("expectOutput", defaults["stop"]["expectOutput"]).toStyledString().c_str());

        afterStop = new SvdShellOperations(
            root["afterStop"].get("commands", defaults["afterStop"]["commands"]).toStyledString().c_str(),
            root["afterStop"].get("expectOutput", defaults["afterStop"]["expectOutput"]).toStyledString().c_str());

        reload = new SvdShellOperations(
            root["reload"].get("commands", defaults["reload"]["commands"]).toStyledString().c_str(),
            root["reload"].get("expectOutput", defaults["reload"]["expectOutput"]).toStyledString().c_str());

        validate = new SvdShellOperations(
            root["validate"].get("commands", defaults["validate"]["commands"]).toStyledString().c_str(),
            root["validate"].get("expectOutput", defaults["validate"]["expectOutput"]).toStyledString().c_str());


    } catch (std::exception &e) {
        cerr << "DUPA1" << endl;
    } catch (...) {
        cerr << "DUPA1" << endl;
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
