/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "webapp_deployer.h"



SvdWebAppDeployer::SvdWebAppDeployer(const QString& domain) {
    logInfo() << "Performing webapp deploy for domain:" << domain;

    auto appDetector = new WebAppTypeDetector(getHomeDir() + DEFAULT_DEPLOYER_DIR + "/" + domain);
    this->appType = appDetector->getType();
    this->typeName = appDetector->typeName;
    logDebug() << "Detected application type:" << this->typeName;
    delete appDetector;


}


SvdWebAppDeployer::~SvdWebAppDeployer() {}


