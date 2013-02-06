# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <../Build.mk>


LIB_FANN_LIB			= ../libfann.so
LIB_FANN_OBJS			= fann.o fann_train_data.o fann_train.o fann_io.o fann_error.o fann_cascade.o


all: fann


fann: $(LIB_FANN_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANN_LIB) $(LIB_FANN_OBJS)


clean:
	$(RM) -f $(LIB_FANN) \
		*.o


distclean:
	$(RM) -f $(LIB_FANN_LIB) \
		*.o


.PHONY: all clean distclean
