
KICK_PROG  				= kick
KICK_OBJS  				= kickstart/utils.o kickstart/core.o kickstart/kick.o

SHELL_PROG  			= shell
SHELL_OBJS  			= kickstart/utils.o kickstart/shell.o

LIB_USAGE_LIB			= libusagesys.so
LIB_USAGE_OBJS		= kickstart/utils.o kickstart/usage_sys.o

LIB_STAT_LIB			= libstat.so
LIB_STAT_OBJS			= kickstart/stat.o

LIB_SYSTIME_LIB		= libsystemtime.so
LIB_SYSTIME_OBJS  = kickstart/system_time.o



kick: $(KICK_OBJS)
	$(CXX) $(CFLAGS) $(KICK_OBJS) -o $(KICK_PROG) $(LDFLAGS)


shell: $(SHELL_OBJS)
	$(CXX) $(CFLAGS) $(SHELL_OBJS) -o $(SHELL_PROG) $(LDFLAGS) -lutil


libusage: $(LIB_USAGE_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_USAGE_LIB) -lutil -lprocstat -lkvm


libstat: $(LIB_STAT_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_STAT_LIB)


libsystime: $(LIB_SYSTIME_OBJS)
	$(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_SYSTIME_LIB)
