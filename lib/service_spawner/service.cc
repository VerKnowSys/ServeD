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


/* install software */
void SvdService::installSlot() {
    logDebug() << "Invoked install slot for service:" << name;

    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.installing";
    if (config->serviceInstalled()) {
        logInfo() << "No need to install service" << name << "because it's already installed.";
    } else {
        logDebug() << "Loading service igniter" << name;

        logTrace() << "Launching commands:" << config->install->commands;
        auto proc = new SvdProcess(name, "install");
        touch(indicator);
        proc->spawnProcess(config->install->commands);
        proc->waitForFinished(-1); // no timeout
        proc->kill();

        /* inform output about some kind of a problem */
        if (config->serviceInstalled()) {
            logDebug() << "Found installed file indicator of software:" << config->softwareName << ", which is base for service:" << name;
        } else { /* software wasn't installed, generate error */
            // TODO
        }
        QFile::remove(indicator); // this indicates finish of installing process

        logTrace() << "After proc install execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::configureSlot() {
    logDebug() << "Invoked configure slot for service:" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.configuring";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to configure service" << name << "because it's already configuring.";
    } else {
        logTrace() << "Loading service igniter" << name;
        touch(indicator);
        logTrace() << "Launching commands:" << config->configure->commands;
        auto proc = new SvdProcess(name, "configure");
        proc->spawnProcess(config->configure->commands);
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc configure execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::startSlot() {
    logDebug() << "Invoked start slot for service:" << name;
    uptime->start();

    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.running";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to run service" << name << "because it's already running.";
    } else {
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

        touch(indicator);

        proc->waitForFinished();
        proc->kill();
        logTrace() << "After proc start execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::afterStartSlot() {
    logDebug() << "Invoked after start slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.afterStarting";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to afterStart service" << name << "because it's already afterStarting.";
    } else {
        logTrace() << "Launching commands:" << config->afterStart->commands;
        touch(indicator);
        auto proc = new SvdProcess(name, "afterStart");
        proc->spawnProcess(config->afterStart->commands);
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc afterStart execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::stopSlot() {
    logDebug() << "Invoked stop slot for service:" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.running";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to stop service" << name << "because it's already stopped.";
    } else {
        auto proc = new SvdProcess(name, "stop");
        logInfo() << "Stopping service" << name << "after" << QDateTime::fromTime_t(getUptime()).toString("ss hh DD") << "of uptime.";
        delete uptime;
        uptime = new QElapsedTimer(); // reset uptime count

        logTrace() << "Loading service igniter" << name;
        proc->spawnProcess(config->stop->commands); // invoke igniter stop, and then try to look for service.pid in prefix directory:

        QString servicePidFile = config->prefixDir() + "/service.pid";
        if (QFile::exists(servicePidFile)) {
            uint pid = QString(readFileContents(servicePidFile).c_str()).toUInt();
            logDebug() << "Service pid found:" << QString::number(pid) << "in file:" << servicePidFile;
            kill(pid, SIGTERM);
            logDebug() << "Service terminated.";
        }
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc stop execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::afterStopSlot() {
    logDebug() << "Invoked after stop slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.afterStopping";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to afterStop service" << name << "because it's already afterStopping.";
    } else {
        touch(indicator);
        logTrace() << "Launching commands:" << config->afterStop->commands;
        auto proc = new SvdProcess(name, "afterStop");
        proc->spawnProcess(config->afterStop->commands);
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc afterStop execution:" << name;
        delete proc;
    }
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
    QString indicator = config->prefixDir() + "/.reloading";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to reload service" << name << "because it's already reloading.";
    } else {
        logTrace() << "Launching commands:" << config->reload->commands;
        touch(indicator);
        auto proc = new SvdProcess(name, "reload");
        proc->spawnProcess(config->reload->commands);
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc reload execution:" << name;
        delete proc;
    }
    delete config;
}


void SvdService::validateSlot() {
    logDebug() << "Invoked validate slot for service:" << name;
    logTrace() << "Loading service igniter" << name;
    auto config = new SvdServiceConfig(name);
    QString indicator = config->prefixDir() + "/.validating";
    if (QFile::exists(indicator)) {
        logInfo() << "No need to validate service" << name << "because it's already validating.";
    } else {
        logTrace() << "Launching commands:" << config->validate->commands;
        touch(indicator);
        auto proc = new SvdProcess(name, "validate");
        proc->spawnProcess(config->validate->commands);
        proc->waitForFinished();
        proc->kill();

        QFile::remove(indicator);
        logTrace() << "After proc validate execution:" << name;
        delete proc;
    }
    delete config;
}
