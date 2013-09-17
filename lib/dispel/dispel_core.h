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


#define DISPEL_NODE_PUBLISHER_PORT "12000" // XXX: multiplied by 1k for testing purposes
// #define DISPEL_NODE_SUBSCRIBER_PORT "14"
// #define DISPEL_NODE_FILE_SYNC_PORT "16"

#define DISPEL_NODE_PUBLISHER_ENDPOINT ("tcp://*:" DISPEL_NODE_PUBLISHER_PORT)
#define DISPEL_NODE_PUBLISHER_PAUSE 1000
#define DISPEL_NODE_SUBSCRIBER_ENDPOINT ("tcp://0.0.0.0:" DISPEL_NODE_PUBLISHER_PORT)

#define DISPEL_NODE_IDENTIFICATION_FILE (SYSTEMUSERS_HOME_DIR "/svd-node-id.uuid")
#define DISPEL_NODE_KNOWN_NODES_DIR (SYSTEMUSERS_HOME_DIR "/svd-known-nodes/")
#define UUID_CORRECT_LENGTH 39


#include "nzmqt/AbstractZmqBase.hpp"
#include "nzmqt/nzmqt.hpp"


QString readOrGenerateNodeUuid();
QString zmqVersion();


#include "../globals/globals.h"
#include "../notifications/notifications.h"


#endif
