/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */


#include "utils.h"


void setHomeDir(QString & homeDir) {
    if (getuid() == 0)
        homeDir = QString::fromStdString(SYSTEMUSERS_HOME_DIR);
    else
        homeDir = QString::fromStdString(USERS_HOME_DIR) + QString::number(getuid());
}


void setSoftwareDataDir(QString & softwareDataDir) {
    QString homeDir;
    setHomeDir(homeDir);
    softwareDataDir = homeDir + QString::fromStdString(SOFTWARE_DATA_DIR);
}


void setServiceDataDir(QString & serviceDataDir, const QString & name) {
    QString softwareDataDir;
    setSoftwareDataDir(softwareDataDir);
    serviceDataDir = softwareDataDir + name;
}