/**
 *  @author dmilith, tallica
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "service.h"



SvdService::SvdService(const QString& name) {
    this->name = name;
    config = new SvdServiceConfig(name);
    uptime = new QElapsedTimer();
}


SvdService::~SvdService() {
    logTrace() << "Service was alive for" << getUptime() /1000 << "seconds.";
    delete uptime;
    delete config;
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
}


void SvdService::afterStartSlot() {
    logDebug() << "Invoked after start slot for service:" << name;
}


void SvdService::stopSlot() {
    logDebug() << "Invoked stop slot for service:" << name;
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
