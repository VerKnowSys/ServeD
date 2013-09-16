# ServeD natives - ServeD Distribution Spell - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

QT += core network

TARGET    = ../../bin/svddispel

DEFINES += NZMQT_LIB

HEADERS   += *.h \
            nzmqt/*.hpp

SOURCES   += nzmqt/nzmqt.cpp \
            dispel_core.cc \
            dispel_publisher.cc \
            dispel_subscriber.cc \
            dispel.cc


# Zeromq should be installed with "base" list as superuser!
INCLUDEPATH += /Software/Zeromq/include

LIBS      += /Software/Zeromq/lib/libzmq.a ../../../TheSS/src/libquazip.a ../../../TheSS/src/libjsoncpp.a ../../../TheSS/src/liblogger.a ../../../TheSS/src/libnotifications.a -lz
