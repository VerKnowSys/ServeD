#include "TestJsonLibrary.h"


void TestJsonLibrary::testParse1() {

    SvdServiceConfig *config = new SvdServiceConfig(); /* Load default values */
    QCOMPARE(config->staticPort, -1);
    QCOMPARE(config->staticPort, -2);

     // QString str = "Hello";
}
