# ServeD natives - ServeD Daemonizer Utility - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)


HEADERS   += ../globals/*.h
SOURCES   += ../kickstart/core.cc \
            ../kickstart/utils.cc \
            daemon.cc

LIBS      += -lz
TARGET    = ../../bin/svddaemon