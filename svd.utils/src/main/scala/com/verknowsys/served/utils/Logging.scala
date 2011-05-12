package com.verknowsys.served.utils

trait Logging {
    @transient lazy val log = new Logger(this.getClass.getName)
}

// object Logger {
//     def apply(logger: String) = new Logger(logger)
// }

class Logger(klazz: String) {
    def error(msg: String) { }
    def error(msg: String, attrs: Any*): Unit = error(msg.format(attrs:_*))
    
    def warn(msg: String) { }
    def warn(msg: String, attrs: Any*): Unit = warn(msg.format(attrs:_*))
    
    def info(msg: String) { }
    def info(msg: String, attrs: Any*): Unit = info(msg.format(attrs:_*))
    
    def debug(msg: String) { }
    def debug(msg: String, attrs: Any*): Unit = debug(msg.format(attrs:_*))
    
    def trace(msg: String) { }
    def trace(msg: String, attrs: Any*): Unit = trace(msg.format(attrs:_*))
}
