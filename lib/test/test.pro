
CC = clang
CXX = clang++
CONFIG += qtestlib
TEMPLATE = app
TARGET = ../../test
DEPENDPATH += .
INCLUDEPATH += .

HEADERS += TestJsonLibrary.h \
           ../jsoncpp/json/json.h \
           ../service_spawner/config_loader.h \
           ../globals/globals.h \
           ../service_spawner/service_config.h
SOURCES += ../service_spawner/config_loader.cc \
           ../service_spawner/service_config.cc \
           ../jsoncpp/json_reader.cpp \
           ../jsoncpp/json_writer.cpp \
           ../jsoncpp/json_value.cpp \
           TestJsonLibrary.cc
