import sbt._
import growl._
import extract._
import java.io.File
import reaktor.scct.ScctProject
import org.coffeescript.CoffeeScriptCompile


class ServeD(info: ProjectInfo) extends ParentProject(info) with SimpleScalaProject {
    
    // Projects
    lazy val api            = project("svd.api", "Svd API", new SvdApi(_))
    lazy val cli            = project("svd.cli", "Svd CLI", new SvdCli(_), api)
    lazy val utils          = project("svd.utils", "Svd Utils", new SvdUtils(_), api)
    lazy val core           = project("svd.core", "Svd Core", new SvdCore(_), api, utils)
    lazy val web            = project("svd.web", "Svd Web", new SvdWeb(_), api, utils)
    
    
    class SvdProject(info: ProjectInfo) extends DefaultProject(info) with GrowlingTests with BasicSelfExtractingProject with ScctProject {
        
        val mavenVKS = "maven.verknowsys.com" at "http://maven.verknowsys.com/repository/"
        val specsTest = "org.scala-tools.testing" % "specs_2.9.0.RC5" % "1.6.8-SNAPSHOT" % "test"
        
        override def parallelExecution = true
        override def compileOrder = CompileOrder.JavaThenScala
        override def javaCompileOptions = super.javaCompileOptions ++
            javaCompileOptions("-g:none") ++
            javaCompileOptions("-encoding", "UTF-8") ++
            javaCompileOptions("-source", "1.6") ++
            javaCompileOptions("-target", "1.6") ++
            // javaCompileOptions("-Xlint:unchecked") ++
            javaCompileOptions("-Xlint:deprecation")

        override def compileOptions = super.compileOptions ++
            compileOptions("-Ywarn-dead-code") ++
            compileOptions("-Xshow-phases") ++
            compileOptions("-Xresident") ++
            compileOptions("-g:source") ++
            (MaxCompileErrors(1) :: ExplainTypes :: Unchecked :: Deprecation :: Nil).toSeq
        
        override def installActions = "update" :: "run" :: Nil
        override val growlTestImages = GrowlTestImages(
            Some("project/growl_images/pass.png"),
            Some("project/growl_images/fail.png"),
            Some("project/growl_images/fail.png")
        )

    }
    
    
    class SvdUtils(info: ProjectInfo) extends SvdProject(info) {
        val javaNet = "java.net" at "http://download.java.net/maven/2"
        val commonsio = "commons-io" % "commons-io" % "1.4"
        val messadmin = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
        val jna = "net.java.dev.jna" % "jna" % "3.2.5"
    }


    class SvdApi(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val akkaRemote = akkaModule("remote")
    }


    class SvdCli(info: ProjectInfo) extends SvdProject(info) with assembly.AssemblyBuilder {
        val jlineRepo = "JLine Project Repository" at "http://jline.sourceforge.net/m2rep"
        val jline = "jline" % "jline" % "0.9.9"
        
        lazy val cli = task { None; } dependsOn(run(Array("127.0.0.1", "5555")))
        lazy val assemblyFast = assemblyTask(assemblyTemporaryPath, assemblyClasspath,
                                              assemblyExtraJars, assemblyExclude
                              ) dependsOn(compile) describedAs("Builds an optimized, single-file deployable JAR without running tests, just compile")
        
        override def mainClass = Some("com.verknowsys.served.cli.Runner")
    }
    
    
    class SvdWeb(info: ProjectInfo) extends DefaultWebProject(info) with CoffeeScriptCompile {
        val scalaToolsSnapshots = "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots/"
        val liftVersion = "2.4-SNAPSHOT"
        
        override def jettyWebappPath  = webappPath
        override def compileAction = super.compileAction dependsOn(compileCoffeeScript)
        override def libraryDependencies = Set(
          "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
          "net.liftweb" % "lift-mapper_2.9.0-1" % liftVersion % "compile->default",
          "org.mortbay.jetty" % "jetty" % "6.1.26" % "test->default"
          // "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test->default"
          // "junit" % "junit" % "4.5" % "test->default",
          // "com.h2database" % "h2" % "1.2.138"
        ) ++ super.libraryDependencies
    }

    
    class SvdCore(info: ProjectInfo) extends SvdProject(info) with AkkaProject with assembly.AssemblyBuilder {
        val akkaRepo = "Akka Repo" at "http://akka.io/repository"
        val javaNet = "java.net" at "http://download.java.net/maven/2"
        // val jgitRepository = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
        // val pircbot     = "pircbot" % "pircbot" % "1.4.2"
        // val smackx      = "jivesoftware" % "smackx" % "3.0.4"
        val smack = "jivesoftware" % "smack" % "3.0.4"
        val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "0.11.1-SNAPSHOT" // Move it out
        val akkaRemote = akkaModule("remote")
        val akkaTestKit = akkaModule("testkit")
        val neodatis = "org.neodatis.odb" % "neodatis-odb" % "1.9.30.689"
        val h2 = "com.h2database" % "h2" % "1.3.154"
        
        lazy val assemblyFast = assemblyTask(assemblyTemporaryPath, assemblyClasspath,
                                              assemblyExtraJars, assemblyExclude
                              ) dependsOn(compile) describedAs("Builds an optimized, single-file deployable JAR without running tests, just compile")
        

        lazy val served = task { None } dependsOn(run(Array()))
        lazy val svd = served
        lazy val stress = task {
            import Process._
            val compiler = "/usr/bin/clang"
            val tasks = "cpu_load_gen" :: "disk_load_gen" :: Nil
            val currDir = System.getProperty("user.dir")
            println("Current User Dir: %s".format(currDir))

            tasks.foreach { tsk =>
                val t = new Thread {
                    override def run = {
                        val a = "%s -o %s/svd.torturemachine/src/posix_signals/%s %s/svd.torturemachine/src/posix_signals/%s.c".format(compiler, currDir, tsk, currDir, tsk) !

                        val b = "%s/svd.torturemachine/src/posix_signals/%s".format(currDir, tsk) !

                        None
                    }
                }
                t.start
                t.join
                None
            }
            None
        }
        lazy val notes = task { 
            def filetree(file: File, pattern: String): List[File] = {
                if(file.isDirectory) file.listFiles.toList.flatMap(filetree(_, pattern))
                else if(file.getPath.matches(pattern)) List(file)
                else Nil
            }

            val XXX  = ".*//.*(?i:xxx)(.*):?".r
            val NOTE = ".*//.*(?i:note)(.*):?".r
            val HACK = ".*//.*(?i:hack)(.*):?".r
            val TODO = ".*//.*(?i:todo)(.*):?".r
            val FIXME = ".*//.*(?i:fixme)(.*):?".r

            val Colors = Map(
                "xxx " -> Console.MAGENTA,
                "note" -> Console.YELLOW,
                "hack" -> Console.RED,
                "todo" -> Console.BLUE,
                "fixme" -> Console.YELLOW
            )

            filetree(new File("."), ".*src(?!.*OLD).*\\.scala") flatMap { file =>
                FileUtilities.readString(file, log).right.get.split("\n").zipWithIndex.map { 
                    case (line, i) => line match {
                        case XXX(msg)   => ("xxx ",  file, i+1, msg)
                        case NOTE(msg)  => ("note",  file, i+1, msg)
                        case HACK(msg)  => ("hack",  file, i+1, msg)
                        case TODO(msg)  => ("todo",  file, i+1, msg)
                        case FIXME(msg) => ("fixme", file, i+1, msg)
                        case _ => ("", null, 0, "")
                    }
                } filter { _._3 != 0 }
            } sort { 
                case ((n1, f1, _, _), (n2, f2, _, _)) => if(n1.compareTo(n2) == 0) f1.compareTo(f2) < 0 else n1.compareTo(n2) < 0
            } foreach { 
                case (name, file, line, msg) => 
                    println("[%s%s%s] %s:%d  %s%s%s".format(Colors(name), name, Console.RESET, file.getPath.replaceAll("src/(main|test)/scala/com/verknowsys/served", "...$1..."), line, Colors(name), msg, Console.RESET))
            }
            None
        }
        lazy val todo = notes

        override def mainClass = Some("com.verknowsys.served.boot")
    }
        
    
}

