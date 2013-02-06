# ServeD natives - © 2013 verknowsys.com
#
# authors:
#		Daniel (dmilith) Dettlaff
#		Michał (tallica) Lipski
#

CC								= clang
CXX								= clang++
RM								= rm
MAKE              = make

CFLAGS						= -Os -fPIC -fPIC -fPIE
LDFLAGS						= -Wl,--enable-new-dtags
LIB_OPTS					= -shared

MODULES           = kickstart fann
