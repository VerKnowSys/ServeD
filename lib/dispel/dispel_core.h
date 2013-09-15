/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#ifndef __DISPEL_CORE__
#define __DISPEL_CORE__


#include <QtCore>
#include <QUuid>

#include "../globals/globals.h"
#include "../../../TheSS/src/notifications/notifications.h"
#include "../../../TheSS/src/service_spawner/logger.h"
#include "../../../TheSS/src/service_spawner/utils.h"


#define DISPEL_NODE_IDENTIFICATION_FILE (SYSTEMUSERS_HOME_DIR "/svd-node-id.uuid")
#define UUID_CORRECT_LENGTH 39


QString readOrGenerateNodeUuid();


#endif
