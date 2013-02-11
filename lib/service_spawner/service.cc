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
}


void SvdService::configureSlot() {
    logDebug() << "Invoked configure slot for service:" << name;
}


void SvdService::startSlot() {
    logDebug() << "Invoked start slot for service:" << name;
    uptime->start();

    auto config = new SvdServiceConfig(name);

    logTrace() << "Launching commands:" << config->start->commands;
    auto proc = new SvdProcess(name, "start");
    proc->spawnProcess(config->start->commands);

    touch(config->prefixDir() + "/.running");

    proc->waitForFinished();
    proc->kill();
    logTrace("After proc execution");
    delete proc;
    delete config;
}


void SvdService::afterStartSlot() {
    logDebug() << "Invoked after start slot for service:" << name;
}


void SvdService::stopSlot() {
    logDebug() << "Invoked stop slot for service:" << name;
    auto proc = new SvdProcess(name, "stop");

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
    logTrace("After proc execution");
    delete proc;
    delete config;
}


void SvdService::afterStopSlot() {
    logDebug() << "Invoked after stop slot for service:" << name;
}


void SvdService::restartSlot() {
    logDebug() << "Invoked restart slot for service:" << name;
}


void SvdService::reloadSlot() {
    logDebug() << "Invoked reload slot for service:" << name;
}


void SvdService::validateSlot() {
    logDebug() << "Invoked validate slot for service:" << name;
}
