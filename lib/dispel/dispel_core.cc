
/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#include "dispel_core.h"


QString zmqVersion() {
    int major, minor, patch;
    zmq_version(&major, &minor, &patch);
    return QString::number(major) + "." + QString::number(minor) + "." + QString::number(patch);
}


QString readOrGenerateNodeUuid() {
    /* permission check */
    QString dirOnPath = QFileInfo(DISPEL_NODE_IDENTIFICATION_FILE).dir().absolutePath();
    if (getuid() == 0) {
        logDebug() << "Dir on path:" << dirOnPath;
        if (not QFile::permissions(dirOnPath).testFlag(QFile::ExeOwner)) {
            logFatal() << "Insufficient permissions to traverse through directory:" << dirOnPath << ":" << QFile::permissions(dirOnPath).testFlag(QFile::ExeOwner) << "," << QFile::permissions(dirOnPath).testFlag(QFile::ExeUser) << "," << QFile::permissions(dirOnPath).testFlag(QFile::ExeOther);
        } else {
            logDebug() << "Permissions granted to traverse through directory:" << dirOnPath;
        }
        if (not QFile::permissions(DISPEL_NODE_IDENTIFICATION_FILE).testFlag(QFile::ReadOwner)) {
            logFatal() << "Can't read Node ID from file:" << DISPEL_NODE_IDENTIFICATION_FILE << "Check access rights to this file for current user and try again!";
        } else {
            logDebug() << "Permissions granted to read file:" << DISPEL_NODE_IDENTIFICATION_FILE;
        }
    } else {
        logWarn() << "Launching Dispel as non root user. I assume it's just development build to test something out.";
        QString develNodeName = "{devel-node-uuid}";
        logWarn() << "Temporarely set Node ID to:" << develNodeName;
        return develNodeName;
    }

    QString content = "\0";
    if (QFile::exists(QString(DISPEL_NODE_IDENTIFICATION_FILE))) {
        logDebug() << "Permissions to read file:" << DISPEL_NODE_IDENTIFICATION_FILE << ":" << QFile::permissions(DISPEL_NODE_IDENTIFICATION_FILE).testFlag(QFile::ReadOwner) << "," << QFile::permissions(DISPEL_NODE_IDENTIFICATION_FILE).testFlag(QFile::ReadUser) << "," << QFile::permissions(DISPEL_NODE_IDENTIFICATION_FILE).testFlag(QFile::ReadOther);

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
