/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __UTILS_H__
#define __UTILS_H__

#include "../globals/globals.h"
#include <QString>


void setHomeDir(QString & homeDir);
void setSoftwareDataDir(QString & softwareDataDir);
void setServiceDataDir(QString & serviceDataDir, const QString & name);

#endif