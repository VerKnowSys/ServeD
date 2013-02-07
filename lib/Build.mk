# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

DEVEL             = false
DARWIN            = false

CC                = clang
CXX               = clang++
AR                = ar
RM                = rm
QMAKE             = qmake
STRIP             = strip
BIN_OPTS          = -fPIE
LIB_OPTS					= -shared
LDFLAGS           =


.if $(DARWIN) == true
MAKE              = bsdmake
QMAKE_OPTS        = -spec darwin-g++
LIB_POSTFIX       = .dylib
.else
MAKE              = make
LIB_POSTFIX       = .so
.endif


.if $(DEVEL) == true
CFLAGS            = -O0 -g -fPIC -DDEVEL
CXXFLAGS          = -O0 -g -fPIC -DDEVEL
.else
CFLAGS            = -Os -fPIC
CXXFLAGS          = -Os -fPIC
.endif


MODULES           = kickstart fann jsoncpp service_spawner
