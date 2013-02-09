#ifndef __JSON_TEST_CONFIG__
#define __JSON_TEST_CONFIG__

#include "../jsoncpp/json/json.h"
#include "../service_spawner/config_loader.h"
#include "../service_spawner/service_config.h"
#include "../service_spawner/utils.h"

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


};

QTEST_APPLESS_MAIN(TestLibrary)
#include "moc_TestLibrary.cpp"

#endif