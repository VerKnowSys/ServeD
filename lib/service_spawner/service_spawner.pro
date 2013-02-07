# ServeD natives - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

TEMPLATE  = app
QT -= gui
HEADERS   += ../kickstart/core.h ../jsoncpp/json/json.h
SOURCES   += service_spawner.cc
LIBS      += ../libjson.a
TARGET    = ../../ss

