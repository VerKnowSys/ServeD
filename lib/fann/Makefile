# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <../Build.mk>


LIB_FANN_LIB				= ../libfann$(LIB_POSTFIX)
LIB_FANN_OBJS				= fann.o fann_train_data.o fann_train.o fann_io.o fann_error.o fann_cascade.o

LIB_FANNFIXED_LIB		= ../libfann-fixed$(LIB_POSTFIX)
LIB_FANNFIXED_OBJS	= fixedfann.o

LIB_FANNFLOAT_LIB		= ../libfann-float$(LIB_POSTFIX)
LIB_FANNFLOAT_OBJS	= floatfann.o

LIB_FANNDOUBLE_LIB	= ../libfann-double$(LIB_POSTFIX)
LIB_FANNDOUBLE_OBJS	= doublefann.o


all: fann fann-fixed fann-float fann-double


fann: $(LIB_FANN_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANN_LIB) $(LIB_FANN_OBJS)


fann-fixed: $(LIB_FANNFIXED_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANNFIXED_LIB) $(LIB_FANNFIXED_OBJS)


fann-float: $(LIB_FANNFLOAT_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANNFLOAT_LIB) $(LIB_FANNFLOAT_OBJS)


fann-double: $(LIB_FANNDOUBLE_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANNDOUBLE_LIB) $(LIB_FANNDOUBLE_OBJS)


strip:
.if $(DARWIN) == false
	$(STRIP) $(LIB_FANN_LIB)
	$(STRIP) $(LIB_FANNFIXED_LIB)
	$(STRIP) $(LIB_FANNFLOAT_LIB)
	$(STRIP) $(LIB_FANNDOUBLE_LIB)
.endif


clean:
	$(RM) -f *.o


distclean:
	$(RM) -f $(LIB_FANN_LIB) \
		$(LIB_FANNFIXED_LIB) \
		$(LIB_FANNFLOAT_LIB) \
		$(LIB_FANNDOUBLE_LIB) \
		*.o


.PHONY: all clean distclean strip
