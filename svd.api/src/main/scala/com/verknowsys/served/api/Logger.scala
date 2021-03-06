/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api

object Logger {
    sealed abstract class Base extends ApiMessage

    // Request
    case class LevelFor(className: String) extends Base
    case object ListEntries extends Base
    case class AddEntry(className: String, level: Levels.Value) extends Base
    case class RemoveEntry(className: String) extends Base
    
    // Response
    case class Level(level: Levels.Value) extends Base
    case class Entries(entries: Map[String, Levels.Value]) extends Base
    
    // Other
    object Levels extends Enumeration {
        val Error, Warn, Info, Debug, Trace = Value
    }
}
