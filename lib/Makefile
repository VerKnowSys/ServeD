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
	cd service_spawner; $(QMAKE) $(QMAKE_OPTS) service_spawner.pro


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



# install:
# 	-for d in $(MODULES); do (cd $$d; $(MAKE) install ); done


# uninstall:
# 	-for d in $(MODULES); do (cd $$d; $(MAKE) uninstall ); done


.PHONY: all clean distclean strip prepare
