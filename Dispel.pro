# ServeD natives - ServeD Distributed Spell - © 2013 verknowsys.com
#
# author:
#   Daniel (dmilith) Dettlaff
#

include(lib/Common.pro)

TEMPLATE = subdirs
CONFIG += ordered
SUBDIRS = lib/fann lib/cutelogger lib/quazip lib/hiredis lib/jsoncpp lib/notifications lib/zeromq lib/dispel

lib/dispel.depends = lib/zeromq
