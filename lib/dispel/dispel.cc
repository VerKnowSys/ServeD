/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#include "dispel.h"
#include "dispel_publisher.h"
#include "dispel_subscriber.h"


QMap<QString, QString> readNodesData() {
    auto map = QMap<QString, QString>();
    // XXX: 2013-09-19 13:17:04 - dmilith - should read "known nodes" file here:
    map.insert("{devel-node-uuid}", "192.168.0.9"); // XXX: local box
    map.insert("{devel-node-uuid}", "192.168.0.10"); // XXX: local vm
    return map;
}


int main(int argc, char *argv[]) {

    QCoreApplication app(argc, argv);
    QTextCodec::setCodecForCStrings(QTextCodec::codecForName(DEFAULT_STRING_CODEC));

    QStringList args = app.arguments();
    QRegExp rxEnableDebug("-d");
    QRegExp rxEnableTrace("-t");
    QRegExp rxPrintVersion("-v");

    bool debug = false, trace = false;
    for (int i = 1; i < args.size(); ++i) {
        if (rxEnableDebug.indexIn(args.at(i)) != -1 ) {
            debug = true;
        }
        if (rxEnableTrace.indexIn(args.at(i)) != -1 ) {
            debug = true;
            trace = true;
        }
        if (rxPrintVersion.indexIn(args.at(i)) != -1) {
            logInfo() << "ServeD Dispel v" << APP_VERSION << ". " << COPYRIGHT;
            return EXIT_SUCCESS;
        }
    }

    /* Logger setup */
    ConsoleAppender *consoleAppender = new ConsoleAppender();
    assert(consoleAppender);
    Logger::registerAppender(consoleAppender);
    consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] <%c:(%F:%i)> %m\n");
    if (trace && debug)
        consoleAppender->setDetailsLevel(Logger::Trace);
    else if (debug && !trace)
        consoleAppender->setDetailsLevel(Logger::Debug);
    else {
        consoleAppender->setDetailsLevel(Logger::Debug); // INFO!
        consoleAppender->setFormat("%t{dd-HH:mm:ss} [%-7l] %m\n");
    }

    uint uid = getuid();
    switch (uid) {
        case 0: {
            logInfo() << "Checking existance and access priviledges of ServeD private directories";
            readOrGenerateNodeUuid();
            if (not QDir().exists(DISPEL_NODE_KNOWN_NODES_DIR))
                QDir().mkpath(DISPEL_NODE_KNOWN_NODES_DIR);
            chmod(DISPEL_NODE_IDENTIFICATION_FILE, 0600); /* rw------- */
            chmod(DISPEL_NODE_KNOWN_NODES_DIR, 0600); /* rw------- */

        } break;

        default:
            logError() << "Please note that this software requires super user priviledges to run!";
            // logFatal()
    }

    logInfo("The ServeD Dispel v" + QString(APP_VERSION) + ". " + QString(COPYRIGHT));
    logInfo("Using Zeromq v" + zmqVersion());

    /* will create context with polling enabled by default */
    QScopedPointer<ZMQContext> context(createDefaultContext());
    assert(context);
    context->start();

    const QString nodeID = readOrGenerateNodeUuid();

    /* launch local publisher */
    Publisher *publisher = new Publisher(*context, DISPEL_NODE_PUBLISHER_ENDPOINT, nodeID); /* use node id, as channel name */
    assert(publisher);
    publisher->start();

    /* read remote nodes list */
    auto subscribers = QList<Subscriber*>();

    const auto map = readNodesData();
    Q_FOREACH(auto uuid, map.keys()) {
        QString nodeAddress = "tcp://" + map.value(uuid) + ":" + DISPEL_NODE_PUBLISHER_PORT;
        logInfo() << "Subscribing to Node ID:" << uuid << "->" << nodeAddress;

        Subscriber *subscriber = new Subscriber(*context, nodeAddress, uuid); /* use uuid id, as channel name */
        assert(subscriber);
        subscriber->start();

        subscribers << subscriber;
    }

    logDebug() << "Subscribed Nodes:" << subscribers;
    return app.exec();
}

