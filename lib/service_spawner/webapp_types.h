/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __WEBAPP_TYPES__
#define __WEBAPP_TYPES__


#include "utils.h"


class WebAppType: QObject {
    Q_OBJECT

    public:
        QStringList filesThatShouldExist, filesThatShouldNotExist;
        bool detect(const QString& path);
};


class StaticSiteType: public WebAppType {

    public:
        bool detect(const QString& path);

};


class RailsSiteType: public WebAppType {

    public:
        bool detect(const QString& path);

};


class NodeSiteType: public WebAppType {

    public:
        bool detect(const QString& path);

};

#endif
