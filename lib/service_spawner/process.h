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
        SvdProcess(const QString& serviceName, const QString& prefix);
        void spawnDefaultShell();
        void spawnProcess(const QString& command);

    protected:
        void setupChildProcess();

};


#endif