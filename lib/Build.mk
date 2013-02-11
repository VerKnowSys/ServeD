# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

DEVEL             = false
DARWIN            = false

CCACHE            = ccache
CC                = "$(CCACHE) clang"
CXX               = "$(CCACHE) clang++ -std=c++11"
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


MODULES           = cutelogger kickstart fann jsoncpp service_spawner test
