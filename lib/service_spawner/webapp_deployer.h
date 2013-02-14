/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */


#ifndef __WEBAPP_DEPLOYER__
#define __WEBAPP_DEPLOYER__


#include "../kickstart/core.h"
#include "../jsoncpp/json/json.h"
#include "service_config.h"
#include "webapp_types.h"
#include "utils.h"
#include "service.h"
#include "process.h"
#include <QObject>
#include <QtTest/QtTest>



class SvdWebAppDeployer: QObject {
    Q_OBJECT

    WebAppTypes appType = NoType;

    public:
        SvdWebAppDeployer(const QString& domain);
        ~SvdWebAppDeployer();

};


#endif
