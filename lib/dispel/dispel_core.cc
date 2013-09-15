
/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#include "dispel_core.h"



QString readOrGenerateNodeUuid() {
    QString content = "\0";
    if (QFile::exists(QString(DISPEL_NODE_IDENTIFICATION_FILE))) {

        uint tmpFileSize = QFile(DISPEL_NODE_IDENTIFICATION_FILE).size();
        if (tmpFileSize != UUID_CORRECT_LENGTH) {
            QString error = "\0";
            error += QString("Found malformed, or incorrect Node ID:" ) + readFileContents(DISPEL_NODE_IDENTIFICATION_FILE).trimmed() + QString(" with length:") + QString::number(tmpFileSize) + QString(" Should be ") + QString::number(UUID_CORRECT_LENGTH);

            notification(error, "Dispel", ERROR);
            error = "Node ID destroyed on machine XXX:TODO:FIXME, and will be recreated!";
            notification(error, "Dispel", ERROR);
            QFile::remove(DISPEL_NODE_IDENTIFICATION_FILE);
            return readOrGenerateNodeUuid();
        }

        content = readFileContents(DISPEL_NODE_IDENTIFICATION_FILE).trimmed();
        notification("New ServeD Node, with ID: " + content + " has joined the party.", "Dispel", NOTIFY);

    } else {
        content = QUuid().createUuid().toString().trimmed();
        QString info = QString("ServeD Node isn't defined on this host. Deploying new Node ID: ") + content;
        notification(info, "Dispel", NOTIFY);
        writeToFile(DISPEL_NODE_IDENTIFICATION_FILE, content);
    }
    return content;
}
