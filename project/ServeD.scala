import sbt._
import sbt.Keys._

object BuildSettings {
    val buildSettings = Defaults.defaultSettings ++ Seq(
        organization    := "VerKnowSys",
        version         := "0.2.0",
        scalaVersion    := "2.9.0-1",
        resolvers       := Resolvers.all,
        logLevel        := Level.Info,
        compileOrder    := CompileOrder.JavaThenScala,

        scalacOptions   += "-Ywarn-dead-code",
        scalacOptions   += "-Xresident",
        scalacOptions   += "-g:source",
        scalacOptions   += "-explaintypes",
        scalacOptions   += "-unchecked",
        scalacOptions   += "-deprecation",

        javacOptions     += "-g:none",
        // javacOptions     += "-encoding UTF-8",
        // javacOptions     += "-source 1.6",
        // javacOptions     += "-target 1.6",
        // javacOptions     += "-Xlint:unchecked",
        javacOptions     += "-Xlint:deprecation"
    )
}

object Resolvers {
    val akkaRepo = "Akka Repository" at "http://akka.io/repository"
    val jlineRepo = "JLine Project Repository" at "http://jline.sourceforge.net/m2rep"
    val javaNet = "java.net" at "http://download.java.net/maven/2"
    // val scalaToolsSnapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
    val scalaTools  = "releases" at "http://scala-tools.org/repo-releases"
    // val mediavks = "media vks" at "http://media.verknowsys.com/.m2/repository"
    val jgitRepo = "jgit-repository" at "http://download.eclipse.org/jgit/maven"

    val all = Seq(akkaRepo, jlineRepo, javaNet, scalaTools, jgitRepo)
}

object Dependencies {
    val akkaVersion = "1.1.3"

    val akkaActor = "se.scalablesolutions.akka" % "akka-actor" % akkaVersion
    val akkaRemote = "se.scalablesolutions.akka" % "akka-remote" % akkaVersion
    val akkaTestkit = "se.scalablesolutions.akka" % "akka-testkit" % akkaVersion
    val messadmin = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
    val jna = "net.java.dev.jna" % "jna" % "3.2.5"
    val jline = "jline" % "jline" % "0.9.9"
    val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"
    val expect4j = "net.sourceforge.expectj" % "expectj" % "2.0.1"
    val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "1.0.0.201106090707-r"
    val neodatis = "org.neodatis" % "neodatis-odb" % "1.9.24.679"
    val smack = "jivesoftware" % "smack" % "3.0.4"
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.8"
    val h2 = "com.h2database" % "h2" % "1.3.154"
}

object ServeD extends Build {
    import BuildSettings._
    import Dependencies._

    lazy val root = Project("ServeD", file("."), settings = buildSettings) aggregate(api, cli, utils, core, testing)
    lazy val api = Project("api", file("svd.api"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(akkaRemote)
        )
    )

    lazy val cli = Project("cli", file("svd.cli"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(jline)
        )
    ) dependsOn(api)

    lazy val utils = Project("utils", file("svd.utils"),
        settings = buildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(messadmin, jna)
        )
    ) dependsOn(api, testing % "test")

    lazy val core = Project("core", file("svd.core"),
        settings = buildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(
                h2,
                neodatis,
                jgit,
                expect4j,
                smack
            )
        )
    ) dependsOn(utils, testing % "test")

    lazy val testing = Project("testkit", file("svd.testing"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(specs, scalatest, akkaTestkit)
        )
    )
}
