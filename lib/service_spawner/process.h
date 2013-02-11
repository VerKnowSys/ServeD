/**
 *  @author tallica
 *
 *   © 2013 - VerKnowSys
 *
 */

#ifndef __PROCESS_H__
#define __PROCESS_H__

#include "../globals/globals.h"
#include "utils.h"

#include <QProcess>


class SvdProcess: public QProcess {

    public:
        SvdProcess();
        void spawnDefaultShell();
        void spawnService(const char *command);

    protected:
        void setupChildProcess();

};


#endif