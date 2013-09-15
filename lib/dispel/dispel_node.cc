/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#include "dispel_node.h"


DispelNode::DispelNode() {
    nodeUuid = readOrGenerateNodeUuid();
    logInfo() << "Launching Node id:" << nodeUuid;
}


QString DispelNode::id() {
    return nodeUuid;
}