# ServeD natives - low level notification mechanism
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

QT += core
TARGET = ../notifications
TEMPLATE = lib
CONFIG += staticlib

HEADERS   += notifications.h
SOURCES   += notifications.cc

mac {
      LIBS      += ../liblogger.a -lz -lncurses
} else {
      LIBS      += ../liblogger.a -lz -lncursesw
}
