# ServeD natives - ServeD Runner Utility - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)


HEADERS   += ../globals/*.h
SOURCES   += ../kickstart/core.cc \
            ../kickstart/utils.cc \
            run.cc

LIBS      += -lz
TARGET    = ../../bin/svdrunner