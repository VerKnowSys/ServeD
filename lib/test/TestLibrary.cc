#include "TestLibrary.h"


/* test utilities */

void writeSampleOf(const char* sample, const char* file) {
    int lfp = open(file, O_RDWR | O_CREAT, 0600);
    write(lfp, sample, strlen(sample));
    close(lfp);
}

/* eof test utilities */

TestLibrary::TestLibrary() {
    /* Logger setup */
    auto *consoleAppender = new ConsoleAppender();
    consoleAppender->setFormat("%t{yyyy-MM-dd HH:mm:ss} [%-7l] <%c> %m\n");
    Logger::registerAppender(consoleAppender);
    QTextCodec::setCodecForCStrings(QTextCodec::codecForName("utf8"));
}

/* test functions */

void TestLibrary::testParseJSONRedis() {
    auto config = new SvdServiceConfig("Redis"); /* Load app specific values */
    QCOMPARE(config->name, QString("Redis"));
    QCOMPARE(config->softwareName, QString("Redis"));
    QCOMPARE(config->staticPort, -1);

    QCOMPARE(config->uid, getuid());

    QVERIFY(config->schedulerActions->first()->cronEntry.contains("*"));
    logDebug() << config->schedulerActions->first()->cronEntry;
    logDebug() << config->schedulerActions->first()->commands;

    /* verify replaceAllIn result, should not contain SERVICE_PORT, SERVICE_DOMAIN, SERVICE_ROOT, SERVICE_ADDRESS */
    QVERIFY(!config->install->commands.contains("SERVICE_PORT"));
    QVERIFY(!config->start->commands.contains("SERVICE_PORT"));
    QVERIFY(!config->configure->commands.contains("SERVICE_PORT"));
    QVERIFY(!config->afterStart->commands.contains("SERVICE_PORT"));

    QVERIFY(!config->install->commands.contains("SERVICE_ROOT"));
    QVERIFY(!config->start->commands.contains("SERVICE_ROOT"));
    QVERIFY(!config->configure->commands.contains("SERVICE_ROOT"));
    QVERIFY(!config->afterStart->commands.contains("SERVICE_ROOT"));

    QVERIFY(!config->install->commands.contains("SERVICE_DOMAIN"));
    QVERIFY(!config->start->commands.contains("SERVICE_DOMAIN"));
    QVERIFY(!config->configure->commands.contains("SERVICE_DOMAIN"));
    QVERIFY(!config->afterStart->commands.contains("SERVICE_DOMAIN"));

    QVERIFY(!config->install->commands.contains("SERVICE_ADDRESS"));
    QVERIFY(!config->start->commands.contains("SERVICE_ADDRESS"));
    QVERIFY(!config->configure->commands.contains("SERVICE_ADDRESS"));
    QVERIFY(!config->afterStart->commands.contains("SERVICE_ADDRESS"));

    delete config;
}


void TestLibrary::testParseDefault() {
    auto *config = new SvdServiceConfig(); /* Load default values */
    QCOMPARE(config->staticPort, -1);
    QVERIFY(config->schedulerActions->length() == 0);
    delete config;
}


void TestLibrary::testMultipleConfigsLoading() {
    auto *config = new SvdServiceConfig(); /* Load default values */
    QVERIFY(config->name == "Default");
    QVERIFY(config->install->commands.length() == 0);
    QVERIFY(config->schedulerActions->length() == 0);
    QVERIFY(config->watchPort == true);
    delete config;

    config = new SvdServiceConfig("Redis");
    QCOMPARE(config->name, QString("Redis"));
    QVERIFY(config->install->commands.length() > 0);
    QVERIFY(config->watchPort == true);
    delete config;

    config = new SvdServiceConfig("Mosh");
    QVERIFY(config->name == "Mosh");
    QVERIFY(config->softwareName == "Mosh");
    QVERIFY(config->install->commands.length() == 0);
    QVERIFY(config->watchPort == false);
    delete config;
}


void TestLibrary::testNonExistantConfigLoading() {
    auto *config = new SvdServiceConfig("PlewisŚmiewis");
    QVERIFY(config->name == "PlewisŚmiewis");
    QVERIFY(config->install->commands.length() == 0);
    QVERIFY(config->watchPort == true);
    delete config;
}


void TestLibrary::testFreePortFunctionality() {
    uint port = registerFreeTcpPort();
    QVERIFY(port != 0);
    logDebug() << "Port:" << port;

    uint port2 = registerFreeTcpPort();
    QVERIFY(port2 != 0);
    QVERIFY(port2 != port); // shouldn't be same if randomize seed is fine
    logDebug() << "Port:" << port2;

    uint takenPort = registerFreeTcpPort(22); // XXX: not yet determined used port.. so using ssh default port
    logDebug() << "Port:" << takenPort;
    QVERIFY(takenPort != 22);
    QVERIFY(takenPort != 0);
}


void TestLibrary::testJSONParse() {
    const char* fileName = "/tmp/test-file-TestJSONParse.json";
    QString value = "";
    int valueInt = -1;

    writeSampleOf("{\"somekey\": \"somevalue\"}", fileName);
    QFile file(fileName);
    if (!file.exists()) {
        QFAIL("JSON file should exists.");
    }
    file.close();

    auto parsed = parseJSON(fileName);
    value = parsed->get("somekey", "none").asString().c_str();
    QVERIFY(value == QString("somevalue"));

    value = parsed->get("someNOKEY", "none").asString().c_str();
    QVERIFY(value == QString("none"));

    valueInt = parsed->get("someNOKEY", 12345).asInt();
    QVERIFY(valueInt == 12345);

    try {
        valueInt = parsed->get("somekey", 12345).asInt();
        QFAIL("It should throw an exception!");
    } catch (std::exception &e) {
        QCOMPARE(e.what(), "Type is not convertible to int");
    }
    delete parsed;

    file.deleteLater();
}


void TestLibrary::testMemoryAllocations() {
    int amount = 1;
    logDebug() << "Beginning" << amount << "loops of allocation test.";
    for (int i = 0; i < amount; ++i) {
        auto config = new SvdServiceConfig("Redis"); /* Load app specific values */
        usleep(1000000); // 1000000 - 1s
        delete config;
    }
}


void TestLibrary::testUtils() {
    uid_t uid = getuid();
    QString dir, homeDir, softwareDataDir, serviceDataDir, name = "Redis";

    if (uid == 0)
        homeDir = "/SystemUsers";
    else
        homeDir = QString("/Users/") + QString::number(uid);

    softwareDataDir = homeDir + "/SoftwareData";
    serviceDataDir = softwareDataDir + "/" + name;

    setHomeDir(dir);
    QVERIFY(dir == homeDir);

    setSoftwareDataDir(dir);
    QVERIFY(dir == softwareDataDir);

    setServiceDataDir(dir, name);
    QVERIFY(dir == serviceDataDir);
}

