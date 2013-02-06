# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

DEVEL             = false
DARWIN            = false

CC                = clang
CXX               = clang++
RM                = rm
MAKE              = make
STRIP             = strip
BIN_OPTS          = -fPIE
LIB_OPTS					= -shared
LDFLAGS           =


.if $(DARWIN) == true
LIB_POSTFIX       = .dylib
.else
LIB_POSTFIX       = .so
.endif


.if $(DEVEL) == true
CFLAGS            = -O0 -fPIC -DDEVEL
CXXFLAGS          = -O0 -fPIC -DDEVEL
.else
CFLAGS            = -Os -fPIC
CXXFLAGS          = -Os -fPIC
.endif


MODULES           = kickstart fann
