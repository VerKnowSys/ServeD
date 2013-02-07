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
