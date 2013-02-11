/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __SERVICE_WATCHER_H__
#define __SERVICE_WATCHER_H__

#include "file_events_manager.h"
#include "service_config.h"
#include "service.h"

#include <QObject>
#include <QFile>
#include <QEventLoop>


class SvdHookTriggerFile: public QFile {

    public:
        explicit SvdHookTriggerFile(const QString& name) : QFile(name) {}
        bool touch();

};


class SvdHookIndicatorFile: public QFile {

    public:
        explicit SvdHookIndicatorFile(const QString& name) : QFile(name) {}

};


class SvdHookTriggerFiles {

    public:
        SvdHookTriggerFiles(const QString& path);
        ~SvdHookTriggerFiles();

        SvdHookTriggerFile *install, *configure, *start, *stop, *restart, *reload, *validate;

};


class SvdHookIndicatorFiles {

    public:
        SvdHookIndicatorFiles(const QString& path);
        ~SvdHookIndicatorFiles();

        SvdHookIndicatorFile *running, *autostart;

};


class SvdServiceWatcher: public QObject {
    Q_OBJECT

    public:
        SvdServiceWatcher(const QString& name);
        ~SvdServiceWatcher();

    private:
        QEventLoop *loop;
        SvdFileEventsManager *fileEvents;
        SvdHookTriggerFiles *triggerFiles;
        SvdHookIndicatorFiles *indicatorFiles;
        QString dataDir;
        SvdService *service;

        void cleanupTriggerHookFiles();

    signals:
        void installService();
        void configureService();
        void validateService();
        void startService();
        void stopService();
        void restartService();
        void reloadService();

    public slots:
        void dirChangedSlot(const QString& dir);
        void fileChangedSlot(const QString& file);

};


#endif