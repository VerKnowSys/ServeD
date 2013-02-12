/**
 *  @author dmilith, tallica
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "service.h"
#include "process.h"


SvdService::SvdService(const QString& name) {
    this->name = name;
    this->uptime = new QElapsedTimer();
    logTrace() << "Creating SvdService with name" << this->name;
}


SvdService::~SvdService() {
    logTrace() << "Service was alive for" << getUptime() /1000 << "seconds.";
    delete uptime;
}


qint64 SvdService::getUptime() {
    return uptime->elapsed() / 1000; // seconds
}


void SvdService::installSlot() {
    logDebug() << "Invoked install slot for service:" << name;

    logDebug() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->install->commands;
    auto proc = new SvdProcess(name, "install");
    touch(config->prefixDir() + "/.installing");
    proc->spawnProcess(config->install->commands);
    proc->waitForFinished();
    proc->kill();

    while (!config->serviceInstalled()) {
        sleep(1);
    }
    logDebug() << "Found installed file indicator of software:" << config->softwareName << ", which is base for service:" << name;
    QFile::remove(config->prefixDir() + "/.installing");

    logTrace() << "After proc install execution:" << name;
    delete proc;
    delete config;
}


void SvdService::configureSlot() {
    logDebug() << "Invoked configure slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->configure->commands;
    auto proc = new SvdProcess(name, "configure");
    proc->spawnProcess(config->configure->commands);

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc configure execution:" << name;
    delete proc;
    delete config;
}


void SvdService::startSlot() {
    logDebug() << "Invoked start slot for service:" << name;
    uptime->start();

    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    if (!config->serviceInstalled()) {
        logInfo() << "Service" << name << "isn't yet installed. Proceeding with installation.";
        installSlot();
        logInfo() << "Service" << name << "isn't yet configured. Proceeding with configuration.";
        configureSlot();
    }
    logInfo() << "Validating service" << name;
    validateSlot(); // invoke validation before each startSlot

    logInfo() << "Launching service" << name;
    logTrace() << "Launching commands:" << config->start->commands;
    auto proc = new SvdProcess(name, "start");
    proc->spawnProcess(config->start->commands);

    touch(config->prefixDir() + "/.running");

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc start execution:" << name;
    delete proc;
    delete config;
}


void SvdService::afterStartSlot() {
    logDebug() << "Invoked after start slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->afterStart->commands;
    auto proc = new SvdProcess(name, "afterStart");
    proc->spawnProcess(config->afterStart->commands);

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc afterStart execution:" << name;
    delete proc;
    delete config;
}


void SvdService::stopSlot() {
    logDebug() << "Invoked stop slot for service:" << name;
    auto proc = new SvdProcess(name, "stop");

    logInfo() << "Stopping service" << name << "after" << getUptime() << "seconds";
    delete uptime;
    uptime = new QElapsedTimer();

    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);
    proc->spawnProcess(config->stop->commands); // invoke igniter stop, and then try to look for service.pid in prefix directory:

    QString servicePidFile = config->prefixDir() + "/service.pid";
    if (QFile::exists(servicePidFile)) {
        uint pid = QString(readFileContents(servicePidFile).c_str()).toUInt();
        logDebug() << "Service pid found:" << QString::number(pid) << "in file:" << servicePidFile;
        kill(pid, SIGTERM);
        logDebug() << "Service terminated.";
    }
    QFile::remove(config->prefixDir() + "/.running");

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc stop execution:" << name;
    delete proc;
    delete config;
}


void SvdService::afterStopSlot() {
    logDebug() << "Invoked after stop slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->afterStop->commands;
    auto proc = new SvdProcess(name, "afterStop");
    proc->spawnProcess(config->afterStop->commands);

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc afterStop execution:" << name;
    delete proc;
    delete config;
}


void SvdService::restartSlot() {
    logDebug() << "Invoked restart slot for service:" << name;
    stopSlot();
    startSlot();
}


void SvdService::reloadSlot() {
    logDebug() << "Invoked reload slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->reload->commands;
    auto proc = new SvdProcess(name, "reload");
    proc->spawnProcess(config->reload->commands);

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc reload execution:" << name;
    delete proc;
    delete config;
}


void SvdService::validateSlot() {
    logDebug() << "Invoked validate slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->validate->commands;
    auto proc = new SvdProcess(name, "validate");
    proc->spawnProcess(config->validate->commands);

    proc->waitForFinished();
    proc->kill();
    logTrace() << "After proc validate execution:" << name;
    delete proc;
    delete config;
}
