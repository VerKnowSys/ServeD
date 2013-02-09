# ServeD natives - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

QT -= gui
QT += network
HEADERS   += service_config.h \
             utils.h \
             ../kickstart/core.h \
             ../jsoncpp/json/json.h \
             ../cutelogger/Logger.h \
             ../cutelogger/ConsoleAppender.h \
             ../cutelogger/FileAppender.h
SOURCES   += service_config.cc \
             utils.cc \
             service_spawner.cc
LIBS      += ../libjson.a ../liblogger.a
TARGET    = ../../ss
