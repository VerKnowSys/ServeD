# ServeD natives - ServeD Kickstart - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

QT += network
HEADERS   += *.h
SOURCES   += core.cc \
            utils.cc \
            kick.cc

LIBS      += -lz
TARGET    = ../../bin/svdkick
