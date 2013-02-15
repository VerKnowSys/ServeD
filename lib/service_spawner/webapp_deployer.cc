/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "webapp_deployer.h"



SvdWebAppDeployer::SvdWebAppDeployer(const QString& domain) {
    logInfo() << "Performing webapp deploy for domain:" << domain;

    auto appDetector = new WebAppTypeDetector(getWebAppsDir() + "/" + domain);
    this->appType = appDetector->getType();
    this->typeName = appDetector->typeName;
    this->domain = domain;
    logDebug() << "Detected application type:" << this->typeName;
    delete appDetector;

}


WebAppTypes SvdWebAppDeployer::getType() {
    return this->appType;
}


QString SvdWebAppDeployer::getTypeName() {
    return this->typeName;
}


SvdWebAppDeployer::~SvdWebAppDeployer() {}


void SvdWebAppDeployer::startSlot() {
    logDebug() << "Invoked start slot for:" << typeName << "webapp for domain:" << domain;
}


void SvdWebAppDeployer::stopSlot() {
    logDebug() << "Invoked stop slot for:" << typeName << "webapp for domain:" << domain;
}


void SvdWebAppDeployer::restartSlot() {
    logDebug() << "Invoked restart slot for:" << typeName << "webapp for domain:" << domain;
    stopSlot();
    startSlot();
}


void SvdWebAppDeployer::reloadSlot() {
    logDebug() << "Invoked reload slot for:" << typeName << "webapp for domain:" << domain;
}


