/**
 *  @author tallica, dmilith
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
    autostart   = new SvdHookIndicatorFile(path + "/.autostart");
    running     = new SvdHookIndicatorFile(path + "/.running");
    installing  = new SvdHookIndicatorFile(path + "/.installing");
}


SvdHookIndicatorFiles::~SvdHookIndicatorFiles() {
    delete autostart;
    delete running;
    delete installing;
}


SvdServiceWatcher::SvdServiceWatcher(const QString& name) {
    logDebug() << "Starting SvdServiceWatcher for service:" << name;

    dataDir = getServiceDataDir(name);

    service = new SvdService(name);

    fileEvents = new SvdFileEventsManager();
    fileEvents->registerFile(dataDir);

    triggerFiles = new SvdHookTriggerFiles(dataDir);
    indicatorFiles = new SvdHookIndicatorFiles(dataDir);

    /* connect file event slots to watcher: */
    connect(fileEvents, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
    connect(fileEvents, SIGNAL(fileChanged(QString)), this, SLOT(fileChangedSlot(QString)));

    /* connect watcher signals to slots of service: */
    connect(this, SIGNAL(installService()), service, SLOT(installSlot()));
    connect(this, SIGNAL(configureService()), service, SLOT(configureSlot()));
    connect(this, SIGNAL(validateService()), service, SLOT(validateSlot()));
    connect(this, SIGNAL(startService()), service, SLOT(startSlot()));
    connect(this, SIGNAL(stopService()), service, SLOT(stopSlot()));
    connect(this, SIGNAL(restartService()), service, SLOT(restartSlot()));
    connect(this, SIGNAL(reloadService()), service, SLOT(reloadSlot()));

    connect(qApp, SIGNAL(aboutToQuit()), this, SLOT(shutdownSlot()));

    /* manage service autostart */
    if (indicatorFiles->autostart->exists()) {
        logInfo() << "Performing autostart of service:" << name;
        emit startService();
    }
}


void SvdServiceWatcher::shutdownSlot() {
    qDebug() << "Invoked shutdown slot.";
    qDebug() << "Emitting stopService signal.";
    emit stopService();
}


void SvdServiceWatcher::dirChangedSlot(const QString& dir) {
    logTrace() << "Directory changed:" << dir;

    /* configure */
    if (triggerFiles->configure->exists()) {
        triggerFiles->configure->remove();
        logDebug() << "Emitting configureService() signal.";
        emit configureService();
        return;
    }

    /* validate */
    if (triggerFiles->validate->exists()) {
        triggerFiles->validate->remove();
        logDebug() << "Emitting validateService() signal.";
        emit validateService();
        return;
    }

    /* reload */
    if (triggerFiles->reload->exists()) {
        triggerFiles->reload->remove();
        logDebug() << "Emitting reloadService() signal.";
        emit reloadService();
        return;
    }

    /* install */
    if (triggerFiles->install->exists()) {
        triggerFiles->install->remove();
        if (indicatorFiles->installing->exists())
            logWarn() << "Interrupted emission of installService() signal. Service is currently installing.";
        else {
            logDebug() << "Emitting installService() signal.";
            emit installService();
        }
        return;
    }

    /* start */
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

    /* stop */
    if (triggerFiles->stop->exists()) {
        triggerFiles->stop->remove();
        if (indicatorFiles->running->exists()) {
            logDebug() << "Emitting stopService() signal.";
            emit stopService();
        } else
            logWarn() << "Interrupted emission of stopService() signal. Service is not running.";
        return;
    }

    /* restart */
    if (triggerFiles->restart->exists()) {
        triggerFiles->restart->remove();
        logDebug() << "Emitting restartService() signal.";
        emit restartService();
        return;
    }

    /* Check if directory wasn't removed */
    if (not QFile::exists(dir)) {
        logWarn() << "Service data directory was removed. Shutting down service watcher.";
        emit shutdownSlot();
    }

}


void SvdServiceWatcher::fileChangedSlot(const QString& file) {
    logDebug() << "File changed:" << file;

}


SvdServiceWatcher::~SvdServiceWatcher() {
    delete fileEvents;
    delete triggerFiles;
    delete indicatorFiles;
    delete service;
}
