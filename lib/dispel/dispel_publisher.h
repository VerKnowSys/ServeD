/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#ifndef __DISPEL_NODE__
#define __DISPEL_NODE__


#include "dispel_core.h"


class Publisher: public AbstractZmqBase {
    Q_OBJECT
    typedef AbstractZmqBase super;


    private:
        QString nodeUuid = "\0";
        QString address_;
        QString topic_;
        ZMQSocket* socket_;


    protected:
        QThread* makeExecutionThread(AbstractZmqBase& base) const;

        void startImpl() {
            nodeUuid = readOrGenerateNodeUuid();
            logInfo() << "Launching Publisher with id:" << nodeUuid;
            launchPublisher();
            assert(!nodeUuid.isEmpty());
            QTimer::singleShot(1000, this, SLOT(sendPing()));
        }


    signals:
        void pingSent(const QList<QByteArray>& message);


    protected slots:
        void sendPing() {
            static quint64 counter = 0;
            QList< QByteArray > msg;
            msg += topic_.toLocal8Bit();
            msg += QString("MSG[%1: %2]").arg(++counter).arg(QDateTime::currentDateTime().toLocalTime().toString(Qt::ISODate)).toLocal8Bit();
            assert(socket_);
            socket_->sendMessage(msg);
            logDebug() << "Publisher> " << msg;
            emit pingSent(msg);
            QTimer::singleShot(1000, this, SLOT(sendPing()));
        }


    public:
        explicit Publisher(ZMQContext& context, const QString& address, const QString& topic, QObject* parent = 0): super(parent), address_(address), topic_(topic), socket_(0) {
                assert(context);
                socket_ = context.createSocket(ZMQSocket::TYP_PUB, this);
                assert(socket_);
                socket_->setObjectName("Publisher.Socket.socket(PUB)");
        }

        QString id();
        void launchPublisher();

};


#endif
