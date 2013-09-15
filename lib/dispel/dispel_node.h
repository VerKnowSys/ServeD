/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#ifndef __DISPEL_NODE__
#define __DISPEL_NODE__


#include "dispel_core.h"


class DispelNode {

    private:
        QString nodeUuid = "\0";

    public:
        DispelNode();
        QString id();

};


#endif