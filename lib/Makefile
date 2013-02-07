# ServeD natives - © 2013 verknowsys.com
#
# author:
#		Daniel (dmilith) Dettlaff
#

.include <Build.mk>


all: prepare
	-for d in $(MODULES); do (cd $$d; $(MAKE) ); done


prepare:
	cd service_spawner; $(QMAKE) service_spawner.pro


clean:
	-for d in $(MODULES); do (cd $$d; $(MAKE) clean ); done


distclean:
	-for d in $(MODULES); do (cd $$d; $(MAKE) distclean ); done


strip:
	-for d in $(MODULES); do (cd $$d; $(MAKE) strip ); done



# install:
# 	-for d in $(MODULES); do (cd $$d; $(MAKE) install ); done


# uninstall:
# 	-for d in $(MODULES); do (cd $$d; $(MAKE) uninstall ); done


.PHONY: all clean distclean strip prepare
