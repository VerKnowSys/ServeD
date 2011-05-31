import sbt._
import growl._
import extract._
import java.io.File
import reaktor.scct.ScctProject
import org.coffeescript.CoffeeScriptCompile



class ServeD(info: ProjectInfo) extends ParentProject(info) with SimpleScalaProject {
    
    // Projects
    lazy val sigar          = project("svd.sigar", "SvdSigar")
    lazy val conf           = project("svd.conf", "SvdConfiguration")
    lazy val api            = project("svd.api", "SvdAPI", new SvdApi(_), conf)
    lazy val spechelpers    = project("svd.spechelpers", "SvdSpecHelpers", new SvdSpecHelpers(_))
    lazy val utils          = project("svd.utils", "SvdUtils", new SvdUtils(_), api, conf, spechelpers)
    lazy val cli            = project("svd.cli", "SvdCLI", new SvdCli(_), utils, api)
    lazy val ci             = project("svd.ci", "SvdCI", new SvdCI(_), utils, api)
    lazy val db             = project("svd.db", "SvdDB", new SvdDB(_), utils, api)
    lazy val systemmanager  = project("svd.systemmanager", "SvdSystemManager", new SvdSystemManager(_), utils, api, sigar, db)
    lazy val notifications  = project("svd.notifications", "Notifications", new SvdNotifications(_), utils)
    lazy val maintainer     = project("svd.maintainer", "SvdMaintainer", new SvdMaintainer(_), notifications, systemmanager, api)
    lazy val web            = project("svd.web", "SvdWeb", new SvdWeb(_), conf, utils, api)
    
    // Dependencies
    class SvdProject(info: ProjectInfo) extends DefaultProject(info) with GrowlingTests with BasicSelfExtractingProject with ScctProject {
        
        override def parallelExecution = true
        
        override def compileOrder = CompileOrder.JavaThenScala
        
        override def javaCompileOptions = super.javaCompileOptions ++
            javaCompileOptions("-g:none") ++
            javaCompileOptions("-encoding", "UTF-8") ++
            javaCompileOptions("-source", "1.6") ++
            javaCompileOptions("-target", "1.6") ++
            javaCompileOptions("-Xlint:unchecked") ++
            javaCompileOptions("-Xlint:deprecation")
            
        override def compileOptions = super.compileOptions ++
            compileOptions("-Ywarn-dead-code") ++
            compileOptions("-Xshow-phases") ++
            compileOptions("-Xresident") ++
            compileOptions("-g:source") ++
            // compileOptions("-Xlint:unchecked") ++
            // compileOptions("-Xlint:deprecation") 
            (MaxCompileErrors(1) :: ExplainTypes :: Unchecked :: Deprecation :: Nil).toSeq // ++ compileOptions("-make:changed")
        
        override def installActions = "update" :: "run" :: Nil
        
        override val growlTestImages = GrowlTestImages(
            Some("project/growl_images/pass.png"),
            Some("project/growl_images/fail.png"),
            Some("project/growl_images/fail.png")
        )

        val scalaToolsSnapshots = "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots/"
        val specsTest = "org.scala-tools.testing" % "specs_2.9.0.RC5" % "1.6.8-SNAPSHOT" % "test"
    }
    
    
    class SvdCI(info: ProjectInfo) extends SvdProject(info)
    class SvdSigar(info: ProjectInfo) extends SvdProject(info)
    
    
    class SvdApi(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val akkaRemote  = akkaModule("remote")
    }
    
    class SvdDB(info: ProjectInfo) extends SvdProject(info){
        val neodatis = "org.neodatis.odb" % "neodatis-odb" % "1.9.30.689"
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
    
    
    class SvdSystemManager(info: ProjectInfo) extends SvdProject(info) {
        import Process._
        
        val h2          = "com.h2database" % "h2" % "1.3.154"
        
        override def parallelExecution = false
        
        lazy val stress = task {
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
        
    }
    
    
    class SvdNotifications(info: ProjectInfo) extends SvdProject(info) {
        val smack       = "jivesoftware" % "smack" % "3.0.4"
    }
    
    
    class SvdSpecHelpers(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val commonsio = "commons-io" % "commons-io" % "1.4"
        val specs = "org.scala-tools.testing" % "specs_2.9.0.RC5" % "1.6.8-SNAPSHOT"
    }
    
    
    class SvdUtils(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        // val jgitRepository = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
        val mavenLocal = "teamon.eu" at "http://maven.teamon.eu"
        val javaNet    = "java.net" at "http://download.java.net/maven/2"
        
        val commonsio   = "commons-io" % "commons-io" % "1.4"
        val messadmin   = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
        // val pircbot     = "pircbot" % "pircbot" % "1.4.2"
        // val smackx      = "jivesoftware" % "smackx" % "3.0.4"
        // val j2sshcommon = "sshtools" % "j2ssh-common" % "0.2.2"
        // val j2sshcore   = "sshtools" % "j2ssh-core" % "0.2.2"
        val jgit        = "org.eclipse.jgit" % "org.eclipse.jgit" % "0.11.1-SNAPSHOT" // Move it out
        val jna         = "net.java.dev.jna" % "jna" % "3.2.5"
        val swing       = "org.scala-lang" % "scala-swing" % "2.8.1"
        val akkaRemote  = akkaModule("remote")
        val akkaTestKit = akkaModule("testkit")
    }
    
    
    class SvdMaintainer(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val dispatch = "net.databinder" %% "dispatch-http" % "0.7.8"

        lazy val served = task { None } dependsOn(run(Array()))
        lazy val svd = served
        
        override def mainClass = Some("com.verknowsys.served.boot")
    }
    
    class SvdWeb(info: ProjectInfo) extends DefaultWebProject(info) with CoffeeScriptCompile {
        val liftVersion = "2.4-SNAPSHOT"
        
        val snapshotRepo = "ScalaTools Snapshots" at "http://scala-tools.org/repo-snapshots"
        
        override def jettyWebappPath  = webappPath
        
        override def compileAction = super.compileAction dependsOn(compileCoffeeScript)
        
        override def libraryDependencies = Set(
          "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
          // "net.liftweb" % "lift-mapper_2.8.1" % liftVersion % "compile->default",
          "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default",
          "junit" % "junit" % "4.5" % "test->default",
          "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test->default"
          // "com.h2database" % "h2" % "1.2.138"
        ) ++ super.libraryDependencies
    }
    
    
    // Other
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
    
    
    val todo = notes
    
    
}

