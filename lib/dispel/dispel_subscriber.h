/*
    Author: Daniel (dmilith) Dettlaff
    based on fragment of nzmqt implementation by Johann Duscher (a.k.a. Jonny Dee)

    © 2013 - VerKnowSys
*/


#ifndef __DISPEL_SUBSCRIBER__
#define __DISPEL_SUBSCRIBER__


#include "dispel_core.h"


class Subscriber: public AbstractZmqBase {
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
        void pingReceived(const QList<QByteArray>& message);


    protected slots:
        void messageReceived(const QList<QByteArray>& message);


    public:
        explicit Subscriber(ZMQContext& context, const QString& address, const QString& topic, QObject* parent = 0): super(parent), address_(address), topic_(topic), socket_(0) {
                assert(context);
                assert(!address_.isEmpty());
                assert(!topic_.isEmpty());
                socket_ = context.createSocket(ZMQSocket::TYP_SUB, this);
                assert(socket_);
                socket_->setObjectName("Subscriber.Socket.socket(SUB)");
                connect(socket_, SIGNAL(messageReceived(const QList<QByteArray>&)), SLOT(messageReceived(const QList<QByteArray>&)));
                logDebug() << "Subscriber created for topic:" << topic;
        }

        inline QString id() {
            return nodeUuid;
        }

        bool notify(QObject *obj, QEvent *event);

};


#endif
