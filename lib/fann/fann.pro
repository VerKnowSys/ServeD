# ServeD natives - FANN AI library
#

include(../Common.pro)

TARGET = ../fann
TEMPLATE = lib
CONFIG += staticlib

QMAKE_CXXFLAGS -= -Wall
QMAKE_CXXFLAGS += -w

SOURCES += *.c
HEADERS += *.h
