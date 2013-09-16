/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#ifndef __DISPEL_CORE__
#define __DISPEL_CORE__


#include <QtCore>
#include <QUuid>
#include <stdexcept>
#include <QStringList>
#include <QTextStream>
#include <QTimer>


#define DISPEL_NODE_SUBSCRIBE_PORT 12
#define DISPEL_NODE_PUBLISH_PORT 14
#define DISPEL_NODE_FILE_SYNC_PORT 16

#define DISPEL_NODE_IDENTIFICATION_FILE (SYSTEMUSERS_HOME_DIR "/svd-node-id.uuid")
#define DISPEL_NODE_KNOWN_NODES_DIR (SYSTEMUSERS_HOME_DIR "/svd-known-nodes/")
#define UUID_CORRECT_LENGTH 39


QString readOrGenerateNodeUuid();
QString zmqVersion();


#include "../globals/globals.h"
#include "../../../TheSS/src/notifications/notifications.h"
#include "../../../TheSS/src/service_spawner/logger.h"
#include "../../../TheSS/src/service_spawner/utils.h"

#include "nzmqt/nzmqt.hpp"
#include "nzmqt/AbstractZmqBase.hpp"

#include "dispel_publisher.h"

// #include "../nzmqt/include/nzmqt/pubsub/Subscriber.hpp"
// #include "../nzmqt/include/nzmqt/reqrep/Requester.hpp"
// #include "../nzmqt/include/nzmqt/reqrep/Replier.hpp"
// #include "../nzmqt/include/nzmqt/pushpull/Ventilator.hpp"
// #include "../nzmqt/include/nzmqt/pushpull/Worker.hpp"
// #include "../nzmqt/include/nzmqt/pushpull/Sink.hpp"


#endif
