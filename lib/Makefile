# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <Build.mk>


all: prepare
.for d in $(MODULES)
	cd $d; $(MAKE)
.endfor


prepare:
	cd cutelogger; $(QMAKE) $(QMAKE_OPTS) CuteLogger.pro ; cd ..
	cd service_spawner; $(QMAKE) $(QMAKE_OPTS) service_spawner.pro ; cd ..
	cd test; $(QMAKE) $(QMAKE_OPTS) test.pro ; cd ..
	cd cutelogger; $(QMAKE) $(QMAKE_OPTS) CuteLogger.pro ; cd ..


clean:
.for d in $(MODULES)
	cd $d; $(MAKE) clean
.endfor


distclean:
.for d in $(MODULES)
	cd $d; $(MAKE) distclean
.endfor


strip:
.for d in $(MODULES)
	cd $d; $(MAKE) strip
.endfor


test: prepare
	cd test/; $(MAKE)



.PHONY: all clean distclean strip prepare test
