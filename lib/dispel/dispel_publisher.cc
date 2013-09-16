/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#include "dispel_publisher.h"


QThread* Publisher::makeExecutionThread(AbstractZmqBase& base) const {
    QThread* thread = new QThread;
    assert(thread);
    base.moveToThread(thread);

    bool connected = false;
    connected = connect(thread, SIGNAL(started()), &base, SLOT(start()));
    assert(connected);
    connected = connect(&base, SIGNAL(finished()), thread, SLOT(quit()));
    assert(connected);
    connected = connect(&base, SIGNAL(finished()), &base, SLOT(deleteLater()));
    assert(connected);
    connected = connect(thread, SIGNAL(finished()), thread, SLOT(deleteLater()));
    assert(connected);

    return thread;
}


void Publisher::launchPublisher() {
    nodeUuid = readOrGenerateNodeUuid();
    assert(!nodeUuid.isEmpty());
    logInfo() << "Launching Node id:" << nodeUuid;
}


QString Publisher::id() {
    return nodeUuid;
}
