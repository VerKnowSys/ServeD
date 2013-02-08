# ServeD natives - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

QT -= gui
QT += network
HEADERS   += service_config.h config_loader.h ../kickstart/core.h ../jsoncpp/json/json.h
SOURCES   += service_config.cc config_loader.cc service_spawner.cc
LIBS      += ../libjson.a
TARGET    = ../../ss
