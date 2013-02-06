# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#


CC                = clang
CXX               = clang++
RM                = rm
MAKE              = make
DEVEL             = false
LDFLAGS						= -Wl,--enable-new-dtags
LIB_OPTS					= -shared


.if $(DEVEL) == true
CFLAGS            = -O0 -fPIC -fPIE -DDEVEL
CXXFLAGS          = -O0 -fPIC -fPIE -DDEVEL
.else
CFLAGS            = -Os -fPIC -fPIE
CXXFLAGS          = -Os -fPIC -fPIE
.endif


MODULES           = kickstart fann
