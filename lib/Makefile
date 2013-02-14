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


clean:
.for d in $(MODULES)
	cd $d; $(MAKE) clean
.endfor


distclean:
	cd fann; $(MAKE) distclean; cd ..
	cd kickstart; $(MAKE) distclean; cd ..
	cd cutelogger; $(MAKE) clean; $(RM) -rf Makefile ../liblogger.a *.moc; cd ..
	cd service_spawner; $(MAKE) clean; $(RM) -rf Makefile ../../ss *.moc; cd ..
	cd test; $(MAKE) clean; $(RM) -rf Makefile ../../test *.moc; cd ..
	cd jsoncpp; $(MAKE) clean; cd .. ; $(RM) -rf libjson.*


strip:
.for d in $(MODULES)
	cd $d; $(MAKE) strip
.endfor


test:
	cd test/; $(QMAKE) $(QMAKE_OPTS) test.pro ; $(MAKE) ; cd .. ;  ../test -v2 -nocrashhandler 2>/dev/null



.PHONY: all clean distclean strip prepare test
