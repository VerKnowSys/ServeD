import sbt._
import sbt.Keys._


object Tasks {
    import scala.Console._

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

    val notes = TaskKey[Unit]("notes", "Shows code notes")

    val notesTask = notes <<= (moduleName, baseDirectory, unmanagedSources in Compile, unmanagedSources in Test) map {
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

    val cp = TaskKey[Unit]("cp", "Show full classpath")

    val cpTask = cp <<= (fullClasspath in Compile) map { (cp) =>
        println(cp map { _.data } mkString ":")
    }

    val writeCp = TaskKey[Unit]("write-cp", "Write full classpath to ${module}.classhpath file")

    val writeCpTasl = writeCp <<= (moduleName, fullClasspath in Compile) map { (module, cp) =>
        IO.write(file("tmp/" + module + ".classpath"), cp map { _.data } mkString ":")
    }

    def all = Seq(notesTask, cpTask, writeCpTasl)
}
