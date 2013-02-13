#ifndef __JSON_TEST_CONFIG__
#define __JSON_TEST_CONFIG__

#include "../kickstart/core.h"
#include "../jsoncpp/json/json.h"
#include "../service_spawner/service_config.h"
#include "../service_spawner/utils.h"
#include "../service_spawner/service.h"
#include "../service_spawner/process.h"

#include <QObject>
#include <QtTest/QtTest>


class TestLibrary: public QObject {
    Q_OBJECT
        ConsoleAppender* consoleAppender;

        public:
            TestLibrary();
            ~TestLibrary();

        private slots:
            void testParseDefault();
            void testParseJSONRedis();
            void testFreePortFunctionality();
            void testJSONParse();
            void testMemoryAllocations();
            void testMultipleConfigsLoading();
            void testNonExistantConfigLoading();
            void testUtils();
            void testSomeRealCraziness();
            void testSanityValueCheck();

            void testStartingRedis();


};

QTEST_APPLESS_MAIN(TestLibrary)
#include "moc_TestLibrary.cpp"

#endif