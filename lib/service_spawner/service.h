/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_H__
#define __SERVICE_H__

#include "../globals/globals.h"
#include "service_config.h"
#include "utils.h"

#include <QObject>
#include <QElapsedTimer>


class SvdService: public QObject {
    Q_OBJECT

    public:
        SvdService(const QString& name);
        ~SvdService();
        qint64 getUptime();


    private:
        QString name;
        QElapsedTimer *uptime;
        SvdServiceConfig *config;


    public slots:
        void installSlot();
        void configureSlot();
        void startSlot();
        void afterStartSlot();
        void stopSlot();
        void afterStopSlot();
        void restartSlot();
        void reloadSlot();
        void validateSlot();
};


#endif