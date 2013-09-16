/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#include "dispel_publisher.h"


bool Publisher::notify(QObject *obj, QEvent *event) {
    try {
        return notify(obj, event);
    }
    catch (std::exception& ex) {
        logError() << "Exception thrown:" << ex.what();
        return false;
    }
}


void Publisher::startImpl() {
    nodeUuid = readOrGenerateNodeUuid();
    assert(!nodeUuid.isEmpty());
    logInfo() << "Launching Publisher with id:" << nodeUuid << "on address:" << address_;
    socket_->bindTo(address_);
    QTimer::singleShot(DISPEL_NODE_PUBLISHER_PAUSE, this, SLOT(sendPing()));
}


void Publisher::sendPing() {
    static quint64 counter = 0;
    QList< QByteArray > msg;
    msg += topic_.toLocal8Bit();
    msg += QString("MSG[%1: %2]").arg(++counter).arg(QDateTime::currentDateTime().toLocalTime().toString(Qt::ISODate)).toLocal8Bit();
    assert(socket_);
    logDebug() << "Trying to publish message:" << msg;
    socket_->sendMessage(msg);
    logDebug() << "Publisher> " << msg;
    emit pingSent(msg);
    QTimer::singleShot(DISPEL_NODE_PUBLISHER_PAUSE, this, SLOT(sendPing()));
}


// QThread* Publisher::makeExecutionThread(AbstractZmqBase& base) const {
//     QThread* thread = new QThread;
//     assert(thread);
//     base.moveToThread(thread);

//     bool connected = false;
//     connected = connect(thread, SIGNAL(started()), &base, SLOT(start()));
//     assert(connected);
//     connected = connect(&base, SIGNAL(finished()), thread, SLOT(quit()));
//     assert(connected);
//     connected = connect(&base, SIGNAL(finished()), &base, SLOT(deleteLater()));
//     assert(connected);
//     connected = connect(thread, SIGNAL(finished()), thread, SLOT(deleteLater()));
//     assert(connected);

//     return thread;
// }
