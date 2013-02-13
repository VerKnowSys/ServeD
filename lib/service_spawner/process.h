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
        SvdProcess(const QString& name);
        void spawnDefaultShell();
        void spawnProcess(const QString& command);
        QString outputFile;

    protected:
        void setupChildProcess();

};


#endif