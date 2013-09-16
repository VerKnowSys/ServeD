/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2013 - VerKnowSys
*/


#include "dispel_publisher.h"
#include "dispel_subscriber.h"


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
            cout << "ServeD Dispel v" << APP_VERSION << ". " << COPYRIGHT << endl;
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

    uint uid = getuid() + geteuid();
    if (uid != 0) {
        logError() << "This piece of software, requires super user to run!";
        // logFatal() << "Aborted!";
    }

    logInfo("The ServeD Dispel v" + QString(APP_VERSION) + ". " + QString(COPYRIGHT));
    logInfo("Using Zeromq v" + zmqVersion());

    /* will create context with polling enabled by default */
    QScopedPointer<ZMQContext> context(createDefaultContext());
    assert(context);
    context->start();

    Publisher *publisher = new Publisher(*context, DISPEL_NODE_PUBLISHER_ADDRESS, "topiś jakiś!"); //
    assert(publisher);
    publisher->start();

    Subscriber *subscriber = new Subscriber(*context, DISPEL_NODE_SUBSCRIBER_ADDRESS, "topiś jakiś!");
    assert(subscriber);
    subscriber->start();


    return app.exec();
}

