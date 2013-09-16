/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#include "dispel_publisher.h"


QThread* Publisher::makeExecutionThread(AbstractZmqBase& base) const {
    QThread* thread = new QThread;
    base.moveToThread(thread);

    bool connected = false;
    connected = connect(thread, SIGNAL(started()), &base, SLOT(start()));         Q_ASSERT(connected);
    connected = connect(&base, SIGNAL(finished()), thread, SLOT(quit()));         Q_ASSERT(connected);
    connected = connect(&base, SIGNAL(finished()), &base, SLOT(deleteLater())); Q_ASSERT(connected);
    connected = connect(thread, SIGNAL(finished()), thread, SLOT(deleteLater()));   Q_ASSERT(connected);

    return thread;
}


void Publisher::launchPublisher() {
    nodeUuid = readOrGenerateNodeUuid();
    logInfo() << "Launching Node id:" << nodeUuid;
}


QString Publisher::id() {
    return nodeUuid;
}
