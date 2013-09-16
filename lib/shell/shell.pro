# ServeD natives - ServeD Shell with full PTY support - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#   Michał (tallica) Lipski
#

include(../Common.pro)


HEADERS   += ../globals/*.h
SOURCES   += ../kickstart/core.cc \
            ../kickstart/utils.cc \
            shell.cc

QMAKE_CXXFLAGS += -w
LIBS      += -lz
unix:!mac {
  LIBS += -lutil
}
TARGET    = ../../bin/svdshell
