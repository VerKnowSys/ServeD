#include "TestJsonLibrary.h"


void TestJsonLibrary::testParseJSONRedis() {
    SvdServiceConfig *config = new SvdServiceConfig("Redis"); /* Load app specific values */
    QCOMPARE(config->name, QString("Redis"));
    QCOMPARE(config->softwareName, QString("Redis"));
    QCOMPARE(config->staticPort, -1);
    // QCOMPARE(config->configure->commands, QString());
}


void TestJsonLibrary::testParseDefault() {
    SvdServiceConfig *config = new SvdServiceConfig(); /* Load default values */
    QCOMPARE(config->staticPort, -1);
}


void TestJsonLibrary::TestFreePortFunctionality() {
    uint port = registerFreeTcpPort();
    QVERIFY(port != 0);
    cout << "Port: " << port << endl;

    uint port2 = registerFreeTcpPort();
    QVERIFY(port2 != 0);
    cout << "Port: " << port2 << endl;

    uint takenPort = registerFreeTcpPort(22); // XXX: not yet determined used port.. so using ssh default port
    cout << "Port: " << takenPort << endl;
    QVERIFY(takenPort != 22);
}