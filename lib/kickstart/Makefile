# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <../Build.mk>


KICK_PROG  				= ../../kick
KICK_OBJS  				= utils.o core.o kick.o

SHELL_PROG  			= ../../shell
SHELL_OBJS  			= utils.o shell.o

LIB_USAGE_LIB			= ../libusagesys.so
LIB_USAGE_OBJS		= utils.o usage_sys.o

LIB_STAT_LIB			= ../libstat.so
LIB_STAT_OBJS			= stat.o

LIB_SYSTIME_LIB		= ../libsystemtime.so
LIB_SYSTIME_OBJS  = system_time.o


all: kick shell libusage libstat libsystime


kick: $(KICK_OBJS)
	$(CXX) $(CFLAGS) -o $(KICK_PROG) $(LDFLAGS) $(KICK_OBJS)


shell: $(SHELL_OBJS)
	$(CXX) $(CFLAGS) $(SHELL_OBJS) -o $(SHELL_PROG) $(LDFLAGS) -lutil


libusage: $(LIB_USAGE_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_USAGE_LIB) -lutil -lprocstat -lkvm


libstat: $(LIB_STAT_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_STAT_LIB)


libsystime: $(LIB_SYSTIME_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_SYSTIME_LIB)


clean:
	$(RM) -f *.o


distclean:
	$(RM) -f $(KICK_PROG) \
		$(SHELL_PROG) \
		$(LIB_USAGE_LIB) \
		$(LIB_STAT_LIB) \
		$(LIB_SYSTIME_LIB) \
		*.o


.PHONY: all clean distclean
