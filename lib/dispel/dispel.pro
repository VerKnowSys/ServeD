# ServeD natives - ServeD Distribution Spell - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

QT += core network

HEADERS   += *.h
SOURCES   += dispel_core.cc \
            dispel_node.cc \
            dispel.cc


# Zeromq should be installed with "base" list as superuser!
INCLUDEPATH += /Software/Zeromq/include
LIBS      += /Software/Zeromq/lib/libzmq.a ../../../TheSS/src/libquazip.a ../../../TheSS/src/libjsoncpp.a ../../../TheSS/src/liblogger.a ../../../TheSS/src/libnotifications.a -lz
TARGET    = ../../bin/svddispel