
CONFIG += qtestlib
QT -= gui
QT += network
TEMPLATE = app
TARGET = ../../test
DEPENDPATH += .
INCLUDEPATH += .

HEADERS += TestLibrary.h \
           ../jsoncpp/json/json.h \
           ../globals/globals.h \
           ../service_spawner/service_config.h \
           ../service_spawner/utils.h \
           ../cutelogger/Logger.h \
           ../cutelogger/ConsoleAppender.h \
           ../cutelogger/FileAppender.h \
           ../service_spawner/service.h \
           ../service_spawner/process.h
SOURCES += ../service_spawner/service_config.cc \
           ../service_spawner/utils.cc \
           ../jsoncpp/json_reader.cpp \
           ../jsoncpp/json_writer.cpp \
           ../jsoncpp/json_value.cpp \
           ../service_spawner/process.cc \
           ../service_spawner/service.cc \
           TestLibrary.cc
LIBS    += ../liblogger.a
