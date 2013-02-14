/**
 *  @author tallica, dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#include "user_watcher.h"
#include "utils.h"
#include "webapp_deployer.h"


SvdUserHookTriggerFiles::SvdUserHookTriggerFiles(const QString& path) {
    shutdown = new SvdHookTriggerFile(path + "/.shutdown");
}


SvdUserHookTriggerFiles::~SvdUserHookTriggerFiles() {
    delete shutdown;
}


SvdUserHookIndicatorFiles::SvdUserHookIndicatorFiles(const QString& path) {
    autostart = new SvdHookIndicatorFile(path + DEFAULT_SERVICE_AUTOSTART_FILE);
}


SvdUserHookIndicatorFiles::~SvdUserHookIndicatorFiles() {
    delete autostart;
}


void SvdUserWatcher::init(uid_t uid) {
    logDebug() << "Starting SvdUserWatcher for user:" << QString::number(uid);

    this->uid = uid;
    this->homeDir = getHomeDir(uid);
    this->softwareDataDir = getSoftwareDataDir(uid);

    collectServices();
    collectWebApplications();

    fileEvents = new SvdFileEventsManager();
    fileEvents->registerFile(homeDir);
    fileEvents->registerFile(softwareDataDir);
    fileEvents->registerFile(homeDir + DEFAULT_WEBAPPS_DIR);

    triggerFiles = new SvdUserHookTriggerFiles(homeDir);
    indicatorFiles = new SvdUserHookIndicatorFiles(homeDir);

    /* connect file event slots to watcher: */
    connect(fileEvents, SIGNAL(directoryChanged(QString)), this, SLOT(dirChangedSlot(QString)));
    connect(fileEvents, SIGNAL(fileChanged(QString)), this, SLOT(fileChangedSlot(QString)));

    connect(qApp, SIGNAL(aboutToQuit()), this, SLOT(shutdownSlot()));
}


void SvdUserWatcher::collectWebApplications() {

    logDebug() << "Looking for WebApps inside" << homeDir + DEFAULT_WEBAPPS_DIR;
    QStringList oldWebApps = webApps;
    logDebug() << "Previous list of WebApps:" << oldWebApps;
    webApps = QDir(homeDir + DEFAULT_WEBAPPS_DIR).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);
    logDebug() << "Current list of WebApps:" << webApps;

    Q_FOREACH(QString name, webApps) {
        if (not oldWebApps.contains(name)) {
            qDebug() << "Found WebApp:" << name;
            new SvdWebAppDeployer(name);
            // this->serviceWatchers << new SvdServiceWatcher(name);
        }
    }
}


void SvdUserWatcher::collectServices() {
    logDebug() << "Looking for services inside" << softwareDataDir;
    QStringList oldServices = services;
    logDebug() << "Previous list of services:" << oldServices;
    services = QDir(softwareDataDir).entryList(QDir::Dirs | QDir::NoDotAndDotDot, QDir::Name);
    logDebug() << "Current list of services:" << services;

    Q_FOREACH(QString name, services) {
        if (not oldServices.contains(name)) {
            qDebug() << "Found service:" << name;
            this->serviceWatchers << new SvdServiceWatcher(name);
        }
    }
}


SvdUserWatcher::SvdUserWatcher() {
    init(getuid());
}


SvdUserWatcher::SvdUserWatcher(uid_t uid) {
    init(uid);
}


void SvdUserWatcher::shutdownSlot() {
    qDebug() << "Invoked shutdown slot.";
}


void SvdUserWatcher::dirChangedSlot(const QString& dir) {
    logTrace() << "Directory changed:" << dir;

    if (dir == homeDir) {
        return;
    }

    if (dir.contains(DEFAULT_WEBAPPS_DIR)) {
        logInfo() << "Deployer has been triggered with new web application";
        collectWebApplications();
        return;
    }

    if (dir == softwareDataDir) {
        collectServices();
        return;
    }
}


void SvdUserWatcher::fileChangedSlot(const QString& file) {
    logDebug() << "File changed:" << file;
}


SvdUserWatcher::~SvdUserWatcher() {
    delete fileEvents;
    delete triggerFiles;
    delete indicatorFiles;
}
