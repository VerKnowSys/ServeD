/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.cli

import com.verknowsys.served.api.Logger

object LoggerCommands extends Commands {
    def commands(implicit svd: Svd): PartialFunction[Args, Unit] = {
        case "logger" :: xs => xs match {
            case "list" :: Nil =>
                // request(Logger.ListEntries) {
                //     case Logger.Entries(entries) => print(entries.map(e => e._1 + ": " + e._2))
                // }

            case ("remove" | "rm") :: className :: Nil =>
                // request(Logger.RemoveEntry(className)){
                //     case ApiSuccess => info("Entry removed")
                // }

            case className :: level :: Nil =>
                loggerLevels.get(level) match {
                    case Some(lvl) =>
                        // request(Logger.AddEntry(className, lvl)){
                        //     case ApiSuccess => info("Entry added")
                        // }

                    case None =>
                        error("Invalid logger level")
                }



            case _ => println("logger [list|remove]")

        }
    }

    val loggerLevels = Map(
        "error" -> Logger.Levels.Error,
        "warn"  -> Logger.Levels.Warn,
        "info"  -> Logger.Levels.Info,
        "debug" -> Logger.Levels.Debug,
        "trace" -> Logger.Levels.Trace
    )
}
