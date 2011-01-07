import sbt._

class ServeD(info: ProjectInfo) extends ParentProject(info) with SimpleScalaProject {
    // Projects
    lazy val api           = project("svd.api", "ServeD API", new SvdApi(_))
    lazy val cli           = project("svd.cli", "ServeD CLI", new SvdCli(_), api)
    lazy val spechelpers   = project("svd.spechelpers", "ServeD Spec Helpers", new SvdSpecHelpers(_))
    lazy val utils         = project("svd.utils", "ServeD Utils", new SvdUtils(_), spechelpers)
    lazy val systemmanager = project("svd.systemmanager", "ServeD SystemManager", new SvdSystemManager(_), utils)
    lazy val notifications = project("svd.notifications", "ServeD Notifications", new SvdNotifications(_), utils)
    lazy val maintainer    = project("svd.maintainer", "ServeD Maintainer", new SvdMaintainer(_), notifications, systemmanager, api, spechelpers)
    
    override def parallelExecution = true

    // Dependencies
    class SvdProject(info: ProjectInfo) extends DefaultProject(info){
        override def compileOptions = Unchecked :: Deprecation :: Nil
        override def parallelExecution = true
        
        val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6"// % "test"
        val junit     = "junit" % "junit" % "4.5"// % "test"
        def commonsio = "commons-io" % "commons-io" % "1.4"
    }
    
    class SvdApi(info: ProjectInfo) extends SvdProject(info)
    class SvdCli(info: ProjectInfo) extends SvdProject(info)
    class SvdSystemManager(info: ProjectInfo) extends SvdProject(info)
    class SvdNotifications(info: ProjectInfo) extends SvdProject(info)
    
    class SvdSpecHelpers(info: ProjectInfo) extends SvdProject(info){
        val commons = commonsio
    }
    
    class SvdUtils(info: ProjectInfo) extends SvdProject(info){
        val jgitRepository = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
        val javaNet        = "java.net" at "http://download.java.net/maven/2"
        val sigarSource = "org.hyperic" at "http://repository.jboss.org/maven2"
        
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
        val sigar       = "org.hyperic" % "sigar" % "1.6.3.82"
    }
    
    class SvdMaintainer(info: ProjectInfo) extends SvdProject(info){
        val dispatch = "net.databinder" %% "dispatch-http" % "0.7.8"
        
        override def mainClass = Some("com.verknowsys.served.maintainer.Maintainer")
    }
    
    // Other
    
}

