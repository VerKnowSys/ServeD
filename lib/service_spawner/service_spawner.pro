# ServeD natives - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

TEMPLATE  = app
QT -= gui
HEADERS   += config_loader.h ../kickstart/core.h ../jsoncpp/json/json.h
SOURCES   += config_loader.cc service_spawner.cc
LIBS      += ../libjson.a
TARGET    = ../../ss

