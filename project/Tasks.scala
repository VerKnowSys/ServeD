/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

import sbt._
import sbt.Keys._
import sbt.inc.Analysis

object Tasks {
    import scala.Console._

    val notes = TaskKey[Unit]("notes", "Shows code notes")
    val cp = TaskKey[Analysis]("cp", "Write full classpath to ${module}.classpath file")


    val XXX  = ".*//.*(?i:xxx)(.*):?".r
    val NOTE = ".*//.*(?i:note)(.*):?".r
    val HACK = ".*//.*(?i:hack)(.*):?".r
    val TODO = ".*//.*(?i:todo)(.*):?".r
    val FIXME = ".*//.*(?i:fixme)(.*):?".r

    val excludedPaths = Seq(
        ".*OLD.*"
    )

    val Colors = Map(
        "xxx" -> MAGENTA,
        "note" -> YELLOW,
        "hack" -> RED,
        "todo" -> BLUE,
        "fixme" -> YELLOW
    )

    def notesTask = (moduleName, baseDirectory, unmanagedSources in Compile, unmanagedSources in Test) map {
        (module, baseDir, mainFiles, testFiles) =>
            List(mainFiles, testFiles) foreach { files =>
                files filterNot { file =>
                    excludedPaths exists { file.getPath matches _ }
                } foreach { file =>
                    IO.read(file).split("\n").zipWithIndex.collect {
                        case (XXX(msg), i)   => ("xxx",   file, i+1, msg)
                        case (NOTE(msg), i)  => ("note",  file, i+1, msg)
                        case (HACK(msg), i)  => ("hack",  file, i+1, msg)
                        case (TODO(msg), i)  => ("todo",  file, i+1, msg)
                        case (FIXME(msg), i) => ("fixme", file, i+1, msg)
                    } foreach {
                        case (name, file, line, msg) =>
                            println("[%s%s%s] (%s):%s:%d  %s%s%s".format(
                                Colors(name),
                                name,
                                RESET,
                                module,
                                file.getPath.replace(baseDir.getPath, ""),
                                line,
                                Colors(name),
                                msg,
                                RESET))
                            // println("txmt://open/?url=file://" + file.getPath)
                    }
                }
            }
    }

    def cpTask = (moduleName, fullClasspath in Compile) map { (module, cp) =>
        IO.write(file("tmp/" + module + ".classpath"), cp map { _.data } mkString ":")
        Analysis.Empty
    }

    def all: Seq[Setting[_]] = Seq(
        notes <<= notesTask,
        cp <<= cpTask,
        compile <<= cpTask
    )
}
