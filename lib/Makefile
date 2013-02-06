# ServeD natives - © 2013 verknowsys.com
#
# authors:
#		Daniel (dmilith) Dettlaff
#		Michał (tallica) Lipski
#

CC								= clang
CXX								= clang++
RM								= rm
CFLAGS						= -Os -fPIC -fPIC -fPIE
LDFLAGS						= -Wl,--enable-new-dtags
LIB_OPTS					= -shared



all: fann kick shell libusage libstat libsystime


clean:
	$(RM) -f $(KICK_PROG) \
		$(SHELL_PROG) \
		$(LIB_USAGE_LIB) \
		$(LIB_STAT_LIB) \
		$(LIB_SYSTIME_LIB) \
		$(LIB_FANN) \
		*.o \
		kickstart/*.o


.PHONY: all clean
