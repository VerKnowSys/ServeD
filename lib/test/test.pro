
CONFIG += qtestlib
QT -= gui
QT += network
TEMPLATE = app
TARGET = ../../test
DEPENDPATH += .
INCLUDEPATH += .

HEADERS += TestLibrary.h \
           ../jsoncpp/json/json.h \
           ../service_spawner/config_loader.h \
           ../globals/globals.h \
           ../service_spawner/service_config.h \
           ../service_spawner/utils.h \
           ../cutelogger/Logger.h \
           ../cutelogger/ConsoleAppender.h \
           ../cutelogger/FileAppender.h
SOURCES += ../service_spawner/config_loader.cc \
           ../service_spawner/service_config.cc \
           ../service_spawner/utils.cc \
           ../jsoncpp/json_reader.cpp \
           ../jsoncpp/json_writer.cpp \
           ../jsoncpp/json_value.cpp \
           TestLibrary.cc
LIBS    += ../liblogger.a
