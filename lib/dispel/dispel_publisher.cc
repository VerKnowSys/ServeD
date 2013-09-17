/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#include "dispel_publisher.h"


void Publisher::startImpl() {
    nodeUuid = readOrGenerateNodeUuid();
    assert(!nodeUuid.isEmpty());
    logInfo() << "Launching Publisher with id:" << nodeUuid << "on address:" << address_;
    QTimer::singleShot(DISPEL_NODE_PUBLISHER_PAUSE, this, SLOT(sendJobMessage()));
    try {
        socket_->bindTo(address_);
    } catch (std::exception& ex) {
        logError() << "Exception thrown in publisher: " << ex.what();
        logFatal() << "Publisher requires free tcp address:" << address_ << " to work. Exitting!";
    }
}


void Publisher::sendJobMessage() {
    static quint64 counter = 0;
    QList< QByteArray > msg;
    msg += channel_.toLocal8Bit();
    msg += QString("MSG[%1: %2]").arg(++counter).arg(QDateTime::currentDateTime().toLocalTime().toString(Qt::ISODate)).toLocal8Bit();
    assert(socket_);
    logDebug() << "Publishing message:" << msg;
    socket_->sendMessage(msg);
    emit sentJobMessage(msg);
    QTimer::singleShot(DISPEL_NODE_PUBLISHER_PAUSE, this, SLOT(sendJobMessage()));
}
