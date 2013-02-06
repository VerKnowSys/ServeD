LIB_FANN					= libfann.so
LIB_FANN_CFLAGS		= -pedantic
LIB_FANN_OBJS			= fann.o fann_train_data.o fann_train.o fann_io.o fann_error.o fann_cascade.o


fann: $(LIB_FANN_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(LIB_FANN_CFLAGS) $(LIB_OPTS) -o $(LIB_FANN) $(LIB_FANN_OBJS)

