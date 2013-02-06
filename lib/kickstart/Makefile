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

LIB_USAGE_LIB			= ../libusagesys$(LIB_POSTFIX)
LIB_USAGE_OBJS		= utils.o usage_sys.o

LIB_STAT_LIB			= ../libstat$(LIB_POSTFIX)
LIB_STAT_OBJS			= stat.o

LIB_SYSTIME_LIB		= ../libsystemtime$(LIB_POSTFIX)
LIB_SYSTIME_OBJS  = system_time.o


.if $(DARWIN) == false
all: kick shell libusage libstat libsystime
.else
all: kick shell libstat libsystime
.endif


kick: $(KICK_OBJS)
	$(CXX) $(CFLAGS) $(BIN_OPTS) -o $(KICK_PROG) $(LDFLAGS) $(KICK_OBJS)


shell: $(SHELL_OBJS)
	$(CXX) $(CFLAGS) $(BIN_OPTS) -o $(SHELL_PROG) $(LDFLAGS) -lutil $(SHELL_OBJS)


.if $(DARWIN) == false
libusage: $(LIB_USAGE_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_USAGE_LIB) -lutil -lprocstat -lkvm $(LIB_USAGE_OBJS)
.endif


libstat: $(LIB_STAT_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_STAT_LIB) $(LIB_STAT_OBJS)


libsystime: $(LIB_SYSTIME_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_SYSTIME_LIB) $(LIB_SYSTIME_OBJS)


strip:
	$(STRIP) $(KICK_PROG)
	$(STRIP) $(SHELL_PROG)
.if $(DARWIN) == false
	$(STRIP) $(LIB_STAT_LIB)
	$(STRIP) $(LIB_SYSTIME_LIB)
	$(STRIP) $(LIB_USAGE_LIB)
.endif


clean:
	$(RM) -f *.o


distclean:
	$(RM) -f $(KICK_PROG) \
		$(SHELL_PROG) \
		$(LIB_USAGE_LIB) \
		$(LIB_STAT_LIB) \
		$(LIB_SYSTIME_LIB) \
		*.o


.PHONY: all clean distclean strip
