#ifndef __JSON_TEST_CONFIG__
#define __JSON_TEST_CONFIG__

#include "../kickstart/core.h"
#include "../jsoncpp/json/json.h"
#include "../service_spawner/service_config.h"
#include "../service_spawner/utils.h"
#include "../service_spawner/service.h"
#include "../service_spawner/process.h"
#include "../service_spawner/webapp_types.h"
#include "../service_spawner/webapp_deployer.h"

#include <QObject>
#include <QtTest/QtTest>


class TestLibrary: public QObject {
    Q_OBJECT
        ConsoleAppender* consoleAppender;
        QString testDataDir, testDataDir2;


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
            void testInstallingWrongRedis();

            void testWebAppDetection();

};

QTEST_APPLESS_MAIN(TestLibrary)
#include "moc_TestLibrary.cpp"

#endif