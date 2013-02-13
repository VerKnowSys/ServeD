# ServeD natives - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

QT -= gui
QT += network
HEADERS   += service.h \
             process.h \
             service_config.h \
             utils.h \
             service_watcher.h \
             user_watcher.h \
             file_events_manager.h \
             ../kickstart/core.h \
             ../jsoncpp/json/json.h \
             ../cutelogger/Logger.h \
             ../cutelogger/ConsoleAppender.h \
             ../cutelogger/FileAppender.h \
             webapp_deployer.h \
             webapp_types.h
SOURCES   += service.cc \
             process.cc \
             service_config.cc \
             utils.cc \
             service_watcher.cc \
             user_watcher.cc \
             file_events_manager.cc \
             webapp_deployer.cc \
             webapp_types.cc \
             service_spawner.cc
LIBS      += ../libjson.a ../liblogger.a
TARGET    = ../../ss
