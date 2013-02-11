/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "service_watcher.h"
#include "utils.h"


SvdHookTriggerFiles::SvdHookTriggerFiles(const QString& path) {
    install   = new SvdHookTriggerFile(path + "/.install");
    configure = new SvdHookTriggerFile(path + "/.configure");
    start     = new SvdHookTriggerFile(path + "/.start");
    stop      = new SvdHookTriggerFile(path + "/.stop");
    restart   = new SvdHookTriggerFile(path + "/.restart");
    reload    = new SvdHookTriggerFile(path + "/.reload");
    validate  = new SvdHookTriggerFile(path + "/.validate");
}


SvdHookTriggerFiles::~SvdHookTriggerFiles() {
    delete install;
    delete configure;
    delete start;
    delete stop;
    delete restart;
    delete reload;
    delete validate;
}


SvdHookIndicatorFiles::SvdHookIndicatorFiles(const QString& path) {
    autostart = new SvdHookIndicatorFile(path + "/.autostart");
    running   = new SvdHookIndicatorFile(path + "/.running");
}


SvdHookIndicatorFiles::~SvdHookIndicatorFiles() {
    delete autostart;
    delete running;
}


SvdServiceWatcher::SvdServiceWatcher(const QString& name) {
    logDebug() << "Starting SvdServiceWatcher for service:" << name;

    dataDir = getServiceDataDir(name);

    config = new SvdServiceConfig(name);

    loop = new QEventLoop();

    fileEvents = new SvdFileEventsManager();
    fileEvents->registerFile(dataDir);

    triggerFiles = new SvdHookTriggerFiles(dataDir);
    indicatorFiles = new SvdHookIndicatorFiles(dataDir);

    connect(fileEvents, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
    connect(fileEvents, SIGNAL(fileChanged(QString)), this, SLOT(fileChangedSlot(QString)));

    loop->exec();
}

void SvdServiceWatcher::dirChangedSlot(const QString& dir) {
    logTrace() << "Directory changed:" << dir;

    if (triggerFiles->start->exists()) {
        triggerFiles->start->remove();
        if (indicatorFiles->running->exists())
            logWarn() << "Interrupted emission of startService() signal. Service is already running.";
        else {
            logDebug() << "Emitting startService() signal.";
            emit startService();
        }
        return;
    }

    if (triggerFiles->stop->exists()) {
        triggerFiles->stop->remove();
        if (indicatorFiles->running->exists()) {
            logDebug() << "Emitting stopService() signal.";
            emit stopService();
        } else
            logWarn() << "Interrupted emission of stopService() signal. Service is not running.";
        return;
    }
}


void SvdServiceWatcher::fileChangedSlot(const QString& file) {
    logDebug() << "File changed:" << file;

}


SvdServiceWatcher::~SvdServiceWatcher() {
    delete loop;
    delete fileEvents;
    delete triggerFiles;
    delete indicatorFiles;
    delete config;
}
