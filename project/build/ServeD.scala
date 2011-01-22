import sbt._
import growl._
import extract._
import java.io.File
import scala.util.matching.Regex
import scala.collection.mutable.ListBuffer



class ServeD(info: ProjectInfo) extends ParentProject(info) with SimpleScalaProject {
    // Projects
    lazy val conf          = project("svd.conf", "SvdConfiguration")
    lazy val api           = project("svd.api", "SvdAPI", conf)
    lazy val spechelpers   = project("svd.spechelpers", "SvdSpecHelpers", new SvdSpecHelpers(_))
    lazy val utils         = project("svd.utils", "SvdUtils", new SvdUtils(_), conf, spechelpers)
    lazy val cli           = project("svd.cli", "SvdCLI", new SvdCli(_), utils, api)
    lazy val systemmanager = project("svd.systemmanager", "SvdSystemManager", new SvdSystemManager(_), utils)
    lazy val notifications = project("svd.notifications", "Notifications", new SvdNotifications(_), utils)
    lazy val maintainer    = project("svd.maintainer", "SvdMaintainer", new SvdMaintainer(_), notifications, systemmanager, api)
    
    override def parallelExecution = false
    
    // Dependencies
    class SvdProject(info: ProjectInfo) extends DefaultProject(info) with GrowlingTests with BasicSelfExtractingProject {
        override def compileOptions =  Unchecked :: Deprecation :: Nil
        override def parallelExecution = true
        override def installActions = "update" :: "run" :: Nil
        
        val specsTest = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test"
        
        override val growlTestImages = GrowlTestImages(
            Some("project/growl_images/pass.png"),
            Some("project/growl_images/fail.png"),
            Some("project/growl_images/fail.png")
        )
    }
    
    class SvdApi(info: ProjectInfo) extends SvdProject(info)
    
    class SvdCli(info: ProjectInfo) extends SvdProject(info) with assembly.AssemblyBuilder {
        lazy val cli = task { None; } dependsOn(run(Array("127.0.0.1", "5555")))

        lazy val assemblyFast = assemblyTask(assemblyTemporaryPath, assemblyClasspath,
                                              assemblyExtraJars, assemblyExclude
                              ) dependsOn(compile) describedAs("Builds an optimized, single-file deployable JAR without running tests, just compile")
        
        override def mainClass = Some("com.verknowsys.served.cli.Runner")
    }
    
    class SvdSystemManager(info: ProjectInfo) extends SvdProject(info) {
        val sigarSource = "org.hyperic" at "http://repository.jboss.org/maven2"
        val sigar       = "org.hyperic" % "sigar" % "1.6.3.82"
        import Process._
        
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
                t.join
                t.start
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
        val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6"
    }
    
    class SvdUtils(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val jgitRepository = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
        val javaNet        = "java.net" at "http://download.java.net/maven/2"
        
        val commonsio   = "commons-io" % "commons-io" % "1.4"
        val messadmin   = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
        // val pircbot     = "pircbot" % "pircbot" % "1.4.2"
        // val smackx      = "jivesoftware" % "smackx" % "3.0.4"
        // val j2sshcommon = "sshtools" % "j2ssh-common" % "0.2.2"
        // val j2sshcore   = "sshtools" % "j2ssh-core" % "0.2.2"
        val jgit        = "org.eclipse.jgit" % "org.eclipse.jgit" % "0.10.0-SNAPSHOT" // Move it out
        val jna         = "net.java.dev.jna" % "jna" % "3.2.5"
        val swing       = "org.scala-lang" % "scala-swing" % "2.8.1"
        val akkaRemote  = akkaModule("remote")
    }
    
    class SvdMaintainer(info: ProjectInfo) extends SvdProject(info) with AkkaProject {
        val dispatch = "net.databinder" %% "dispatch-http" % "0.7.8"

        lazy val served = task { None } dependsOn(run(Array("--monitor")))
        lazy val svd = served
        lazy val servedSkipSsm = task { None } dependsOn(run(Array("--skip-ssm")))
        
        override def mainClass = Some("com.verknowsys.served.maintainer.SvdMaintainer")
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
        
        type Entry = (File, Int, String)
        
        val data = Map(
            "xxx"  -> new ListBuffer[Entry],
            "note" -> new ListBuffer[Entry],
            "hack" -> new ListBuffer[Entry]
        )

        filetree(new File("."), ".*src(?!.*OLD).*\\.scala") foreach { f =>
            FileUtilities.readString(f, log).right.get.split("\n").zipWithIndex.foreach { 
                case (line, i) => 
                    line match {
                        case XXX(msg) => data("xxx") += ((f, i, msg))
                        case NOTE(msg) => data("note") += ((f, i, msg))
                        case HACK(msg) => data("hack") += ((f, i, msg))
                        case _ =>
                    }
            }
        }
        
        data foreach {
            case (name, list) =>
                list foreach { case (file, i, msg) => println("[%s] %s:%d  %s".format(name, file.getPath, i+1, msg)) }
        }
        
        None
    }
    
}

