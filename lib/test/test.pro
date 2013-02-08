
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
           ../service_spawner/service_config.h
SOURCES += ../service_spawner/config_loader.cc \
           ../service_spawner/service_config.cc \
           ../jsoncpp/json_reader.cpp \
           ../jsoncpp/json_writer.cpp \
           ../jsoncpp/json_value.cpp \
           TestLibrary.cc
