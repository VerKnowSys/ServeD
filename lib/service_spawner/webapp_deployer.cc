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


