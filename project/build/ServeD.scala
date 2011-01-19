import sbt._
import growl._
import extract._


class ServeD(info: ProjectInfo) extends ParentProject(info) with SimpleScalaProject {
    // Projects
    lazy val conf          = project("svd.conf", "ServeD Configuration")
    lazy val api           = project("svd.api", "ServeD API", new SvdApi(_), conf)
    lazy val cli           = project("svd.cli", "ServeD CLI", new SvdCli(_), conf, api)
    lazy val spechelpers   = project("svd.spechelpers", "ServeD Spec Helpers", new SvdSpecHelpers(_), conf)
    lazy val utils         = project("svd.utils", "ServeD Utils", new SvdUtils(_), conf, spechelpers)
    lazy val systemmanager = project("svd.systemmanager", "ServeD SystemManager", new SvdSystemManager(_), conf, utils)
    lazy val notifications = project("svd.notifications", "ServeD Notifications", new SvdNotifications(_), conf, utils)
    lazy val maintainer    = project("svd.maintainer", "ServeD Maintainer", new SvdMaintainer(_), conf, notifications, systemmanager, api, spechelpers)
    
    override def parallelExecution = false
    
    trait SvdAkkaProject extends AkkaProject {
        val akkaRemote = akkaModule("remote")
    }

    // Dependencies
    class SvdProject(info: ProjectInfo) extends DefaultProject(info) with GrowlingTests with BasicSelfExtractingProject {
        override def compileOptions =  Unchecked :: Deprecation :: Nil
        override def parallelExecution = true
        override def installActions = "update" :: "run" :: Nil
        
        val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6"
        def commonsio = "commons-io" % "commons-io" % "1.4"
    }
    
    class SvdApi(info: ProjectInfo) extends SvdProject(info)
    
    class SvdCli(info: ProjectInfo) extends SvdProject(info) with SvdAkkaProject {
        lazy val cli = task { None; } dependsOn(run(Array("127.0.0.1", "5555")))
        
        override def mainClass = Some("com.verknowsys.served.cli.Runner")
    }
    
    class SvdSystemManager(info: ProjectInfo) extends SvdProject(info) {
        val sigarSource = "org.hyperic" at "http://repository.jboss.org/maven2"
        val sigar       = "org.hyperic" % "sigar" % "1.6.3.82"
    }
    
    class SvdNotifications(info: ProjectInfo) extends SvdProject(info)
    
    class SvdSpecHelpers(info: ProjectInfo) extends SvdProject(info) with SvdAkkaProject {
        val commons = commonsio
    }
    
    class SvdUtils(info: ProjectInfo) extends SvdProject(info) with SvdAkkaProject {
        val jgitRepository = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
        val javaNet        = "java.net" at "http://download.java.net/maven/2"
        
        val commons     = commonsio
        val log4j       = "log4j" % "log4j" % "1.2.14"
        val messadmin   = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
        val pircbot     = "pircbot" % "pircbot" % "1.4.2"
        val smack       = "jivesoftware" % "smack" % "3.0.4"
        val smackx      = "jivesoftware" % "smackx" % "3.0.4"
        val j2sshcommon = "sshtools" % "j2ssh-common" % "0.2.2"
        val j2sshcore   = "sshtools" % "j2ssh-core" % "0.2.2"
        val jgit        = "org.eclipse.jgit" % "org.eclipse.jgit" % "0.10.0-SNAPSHOT"
        val jna         = "net.java.dev.jna" % "jna" % "3.2.5"
        val swing       = "org.scala-lang" % "scala-swing" % "2.8.1"
    }
    
    class SvdMaintainer(info: ProjectInfo) extends SvdProject(info) with SvdAkkaProject {
        val dispatch = "net.databinder" %% "dispatch-http" % "0.7.8"

        lazy val served = task { None } dependsOn(run(Array("--monitor")))
        
        override def mainClass = Some("com.verknowsys.served.maintainer.Maintainer")
    }
    
    // Other
    
}

