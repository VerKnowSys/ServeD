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
        QString topic_;
        ZMQSocket* socket_;


    protected:
        // QThread* makeExecutionThread(AbstractZmqBase& base) const;
        void startImpl();


    signals:
        void pingSent(const QList<QByteArray>& message);


    protected slots:
        void sendPing();


    public:
        explicit Publisher(ZMQContext& context, const QString& address, const QString& topic, QObject* parent = 0): super(parent), address_(address), topic_(topic), socket_(0) {
                assert(context);
                assert(!address_.isEmpty());
                assert(!topic_.isEmpty());
                socket_ = context.createSocket(ZMQSocket::TYP_PUB, this);
                assert(socket_);
                socket_->setObjectName("Publisher.Socket.socket(PUB)");
                logDebug() << "Publisher created for topic:" << topic;
        }

        inline QString id() {
            return nodeUuid;
        }

        bool notify(QObject *obj, QEvent *event);

};


#endif
