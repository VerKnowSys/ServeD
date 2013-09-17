
## Authors:
* Daniel (dmilith) Dettlaff (dmilith [at] verknowsys.com). I'm also on #freebsd and #scala.pl @ freenode IRC.


## Contributors:
* Tymon (teamon) Tobolski - lead developer (active between: 2009-2012)
* Michał (tallica) Lipski.


## Thanks to:
* Michał (tallica) Lipski - tasks IRC, XMPP bot, svdshell, and more useful code.
* Marcin (lopex) Mielżyński - Scala and overall help.
* Iwo (db) ? - System, low level, technical help.


## Requirements:

* Java - OpenJDK (recommended, but all Sun-Compatible JVMs should work). >= Latest 1.6, 64-bit (yes, only 64 bits are supported)
* Simple Build Tool (xsbt: https://github.com/harrah/xsbt) >= 0.12.2
* Scala (will be downloaded by sbt automatically) >= 2.10.2
* ZSH Shell (it's just used by default) >= 5.0.0
* Smack library (for XMPP Notifications) >= 3.0.4 (downloaded by sbt automatically)
* pIRCbot library (for IRC Notifications) >= 1.4.2 (will be downloaded by sbt automatically)
* [JRebel](http://zeroturnaround.com/software/jrebel/) for Scala is required for development mode. It's not required in production.


## Currently supported platforms:
* Mac OSX (75% features, development),
* Free BSD (100% features, production)
* (upcoming) Linux (100% features, production)


## Questions?
I'm as "dmilith" on:
* #scala.pl, #scala, #freebsd at irc.freenode.net
* dmilith@verknowsys.com on jabber/mail
* dmilith@me.com on imessage/mail
* dmilith on Skype


## Rules?
Look for detailed rules in POLICIES file of this project.


## Used 3rd party software and licenses info:

* [Sofin](http://verknowsys.github.io/sofin)
* [TheSS](https://github.com/VerKnowSys/TheSS) some code fragments are reused in ServeD from Software Spawner
* [Qt4 4.8.x](http://qt-project.org/downloads) QtCore part
* [Hiredis](https://github.com/redis/hiredis) client library by Salvatore Sanfilippo and Pieter Noordhuis (BSD licensed)
* [JSON CPP](http://jsoncpp.sourceforge.net) implementation with JSON comments support by Baptiste Lepilleur (MIT licensed)
* [QuaZIP](http://quazip.sourceforge.net) by Sergey A. Tachenov and contributors (LGPL licensed)
* [0mq-C++](https://github.com/zeromq/cppzmq) - A C++ binding for 0mq, by 250bpm s.r.o., Botond Ballo, iMatix Corporation
* [nzmqt](http://qt-apps.org/content/show.php/nzmqt?content=148558) - A lightweight C++ Qt binding for 0mq, by Johann Duscher (a.k.a. Jonny Dee)
* [fann](http://leenissen.dk/fann/wp/) - Fast Artificial Neural Network Library, by Steffen Nissen and contributors.
* [CuteLogger](https://gitorious.org/cutelogger/cutelogger) - MT logger implementation by Boris Moiseev (LGPL licensed)

