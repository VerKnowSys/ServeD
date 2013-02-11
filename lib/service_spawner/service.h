/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_H__
#define __SERVICE_H__

#include "service_config.h"
#include <QObject>
#include <QElapsedTimer>


class SvdService: public QObject {
    Q_OBJECT

    public:
        QString *serviceDataDir, *softwareDataDir;
        QElapsedTimer *uptime;
        SvdService(const QString& name);

    private:
        SvdServiceConfig *config;

    public slots:
        void installSlot();
        void configureSlot();
        void startSlot();
        void afterStartSlot();
        void stopSlot();
        void afterStopSlot();
        void reloadSlot();
        void validateSlot();
};


#endif