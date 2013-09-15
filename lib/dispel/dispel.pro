# ServeD natives - ServeD Distribution Spell - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(../Common.pro)


HEADERS   += *.h
SOURCES   += dispel.cc


# Zeromq should be installed with "base" list as superuser.
LIBS      += -L/Software/Zeromq/lib -lz -lzmq
TARGET    = ../../bin/svddispel