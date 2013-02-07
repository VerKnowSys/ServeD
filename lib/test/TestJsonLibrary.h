#ifndef __JSON_TEST_CONFIG__
#define __JSON_TEST_CONFIG__


#include "../jsoncpp/json/json.h"
#include "../service_spawner/config_loader.h"
#include "../service_spawner/service_config.h"

#include <QObject>
#include <QtTest/QtTest>


class TestJsonLibrary: public QObject {
    Q_OBJECT

        private slots:
            void testParse1();


};

QTEST_APPLESS_MAIN(TestJsonLibrary)
#include "moc_TestJsonLibrary.cpp"

#endif