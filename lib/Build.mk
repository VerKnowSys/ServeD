# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

DEVEL             = false
DARWIN            = false

CCACHE            =
CC                = "$(CCACHE) clang -fcolor-diagnostics -Qunused-arguments -Wself-assign"
CXX               = "$(CCACHE) clang++ -std=c++11 -fcolor-diagnostics -Qunused-arguments -Wself-assign"
AR                = ar
RM                = rm
QMAKE             = qmake
STRIP             = strip
BIN_OPTS          = -fPIE
LIB_OPTS					= -shared
LDFLAGS           =


.if $(DARWIN) == true
CFLAGS            = -arch x86_64
CXXFLAGS          = -arch x86_64
MAKE              = bsdmake CC=$(CC) CXX=$(CXX)
QMAKE_OPTS        = -spec darwin-g++
LIB_POSTFIX       = .dylib
.else
MAKE              = make CC=$(CC) CXX=$(CXX)
LIB_POSTFIX       = .so
.endif


.if $(DEVEL) == true
CFLAGS            += -O0 -g -fPIC -DDEVEL
CXXFLAGS          += -O0 -g -fPIC -DDEVEL
.else
CFLAGS            = -Os -fPIC
CXXFLAGS          = -Os -fPIC
.endif


MODULES           = cutelogger kickstart fann
