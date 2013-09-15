# ServeD Common - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#


QT -= gui core

QMAKE_CXX = clang++
QMAKE_CC = clang
QMAKE_CPP = clang++ -E

QMAKE_CFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -fPIE -O0
QMAKE_CXXFLAGS += -fcolor-diagnostics -Wself-assign -fPIC -fPIE -O0

mac {

  QMAKE_CXXFLAGS  += -std=c++11

} else {

  QMAKE_CXXFLAGS  += -w
  CONFIG += link_pkgconfig
  PKGCONFIG = QtCore

}

