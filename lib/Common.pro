# ServeD Common - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#


QT -= gui core

QMAKE_CXX = clang++
QMAKE_CC = clang
QMAKE_CPP = clang++ -E

# production opts:
DEFINES += NDEBUG
QMAKE_CFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -fPIE -Os -w
QMAKE_CXXFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -fPIE -Os

# development opts:
# QMAKE_CFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -O0 -w -gline-tables-only
# QMAKE_CXXFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -O0 -gline-tables-only

mac {

  QMAKE_CXXFLAGS  += -std=c++11

} else {

  QMAKE_CXXFLAGS  += -w
  CONFIG += link_pkgconfig
  PKGCONFIG = QtCore

}

