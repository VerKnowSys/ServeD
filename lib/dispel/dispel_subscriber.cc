/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#include "dispel_subscriber.h"


bool Subscriber::notify(QObject *obj, QEvent *event) {
    try {
        return notify(obj, event);
    }
    catch (std::exception& ex) {
        logError() << "Exception thrown:" << ex.what();
        return false;
    }
}


void Subscriber::messageReceived(const QList<QByteArray>& message) {
    logDebug() << "Subscriber> " << message;
    emit pingReceived(message);
}


void Subscriber::startImpl() {
    nodeUuid = readOrGenerateNodeUuid();
    logInfo() << "Launching Subscriber with id:" << nodeUuid << "trying address:" << address_;
    assert(!nodeUuid.isEmpty());
    socket_->connectTo(address_);
    socket_->subscribeTo(topic_);
    logDebug() << "Subscribing to:" << address_ << "@topic:" << topic_;
}

