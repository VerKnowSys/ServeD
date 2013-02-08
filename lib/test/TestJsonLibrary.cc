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


void writeSampleOf(const char* sample, const char* file) {
    int lfp = open(file, O_RDWR | O_CREAT, 0600);
    write(lfp, sample, strlen(sample));
    close(lfp);
}


void TestJsonLibrary::TestJSONParse() {
    const char* fileName = "/tmp/test-file-TestJSONParse.json";
    QString value = "";
    int valueInt = -1;

    writeSampleOf("{\"somekey\": \"somevalue\"}", fileName);
    QFile file(fileName);
    if (!file.exists()) {
        QFAIL("JSON file should exists.");
    }

    Json::Value parsed = parseJSON(fileName);
    value = parsed.get("somekey", "none").asString().c_str();
    QVERIFY(value == QString("somevalue"));

    value = parsed.get("someNOKEY", "none").asString().c_str();
    QVERIFY(value == QString("none"));

    valueInt = parsed.get("someNOKEY", 12345).asInt();
    QVERIFY(valueInt == 12345);

    try {
        valueInt = parsed.get("somekey", 12345).asInt();
        QFAIL("It should throw an exception!");
    } catch (std::exception &e) {
        QCOMPARE(e.what(), "Type is not convertible to int");
    }

    file.deleteLater();
}

