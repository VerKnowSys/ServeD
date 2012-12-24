package org.slf4j.impl;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;
import scala.collection.mutable.Map;
import scala.collection.mutable.Map$;


class SvdSFL4JLoggerFactory implements ILoggerFactory {

    Map<String, Logger> loggerMap = Map$.MODULE$.empty();

    static SvdSFL4JLoggerFactory INSTANCE = new SvdSFL4JLoggerFactory();

    @Override
    public Logger getLogger(String name) {
        SvdSLF4JLogger slogger = new SvdSLF4JLogger(name);
        loggerMap.put(name, slogger);
        return slogger;
    }

//        loggerMap.get(name);
//            SvdSLF4JLogger slogger = new SvdSLF4JLogger(name);
//            return slogger;
//        });

}
