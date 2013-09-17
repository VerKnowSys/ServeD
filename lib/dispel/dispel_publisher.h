/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#ifndef __DISPEL_PUBLISHER__
#define __DISPEL_PUBLISHER__


#include "dispel_core.h"


class Publisher: public AbstractZmqBase {
    Q_OBJECT
    typedef AbstractZmqBase super;


    private:
        QString nodeUuid;
        QString address_;
        QString channel_;
        ZMQSocket* socket_;


    protected:
        void startImpl();


    signals:
        void sentJobMessage(const QList<QByteArray>& message);


    protected slots:
        void sendJobMessage();


    public:
        explicit Publisher(ZMQContext& context, const QString& address, const QString& channel, QObject* parent = 0): super(parent), address_(address), channel_(channel), socket_(0) {
                assert(context);
                assert(!address_.isEmpty());
                assert(!channel_.isEmpty());
                socket_ = context.createSocket(ZMQSocket::TYP_PUB, this);
                assert(socket_);
                socket_->setObjectName("Publisher.Socket.socket(PUB)");
                logDebug() << "Publisher created for channel:" << channel;
        }

        inline QString id() {
            return nodeUuid;
        }

};


#endif
