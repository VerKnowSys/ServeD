# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <../Build.mk>


LIB_JSON_LIB			= ../libjson.a
LIB_JSON_OBJS			= json_value.o json_reader.o json_writer.o


all: libjson


libjson: $(LIB_JSON_OBJS)
	# $(CXX) $(CFLAGS) $(LDFLAGS) $(LIB_OPTS) -o $(LIB_JSON_LIB) $(LIB_JSON_OBJS)
	$(AR) crv $(LIB_JSON_LIB) $(LIB_JSON_OBJS)


strip:
.if $(DARWIN) == false
	$(STRIP) $(LIB_JSON_LIB)
.endif


clean:
	$(RM) -f *.o


distclean:
	$(RM) -f $(LIB_JSON_LIB) \
		*.o


.PHONY: all clean distclean strip
