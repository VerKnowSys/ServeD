/**
 *  @author dmilith
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __WEBAPP_TYPES__
#define __WEBAPP_TYPES__


#include "utils.h"


enum WebAppTypes {
    StaticSite  = 0x01,
    RailsSite   = 0x02,
    NodeSite    = 0x03,

    NoType      = 0x04 /* NOTE: this one *must* be always last type */
};


class WebAppTypeDetector {

    WebAppTypes appType = NoType;

    public:
        WebAppTypeDetector(const QString& path);
        WebAppTypes getType();
        QString typeName;
};


#endif
