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
        SvdProcess(const QString& name, uid_t uid);
        void spawnDefaultShell();
        void spawnProcess(const QString& command);
        QString outputFile;

    protected:
        void setupChildProcess();

    private:
        uid_t uid;
        void init(const QString& name, uid_t uid);

};


#endif