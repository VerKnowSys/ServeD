# ServeD natives - ServeD Distribution Spell - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)

QT += core

TARGET = ../../bin/svddispel

DEFINES += NZMQT_LIB

HEADERS += nzmqt/*.hpp \
      ../quazip/quazip.h \
      ../cutelogger/AbstractAppender.h \
      *.h

SOURCES += nzmqt/*.cpp \
      dispel_core.cc \
      dispel_publisher.cc \
      dispel_subscriber.cc \
      dispel.cc

LIBS += ../libzeromq.a \
      ../libnotifications.a \
      ../liblogger.a

