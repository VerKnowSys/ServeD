# ServeD natives - ServeD Runner Utility - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

# For debug use:
# DEFINES = DEVEL

HEADERS   += ../globals/*.h \
            ../shell/shellutils.h
SOURCES   += ../kickstart/core.cc \
            ../kickstart/utils.cc \
            ../shell/shellutils.cc \
            run.cc

LIBS      += -lz
TARGET    = ../../bin/svdrunner