import sbt._
import sbt.Keys._

import scala.io.Source
// import com.github.siasia.WebPlugin._
// import coffeescript.CoffeeScript
import sbtassembly._
import sbtassembly.Plugin._
import AssemblyKeys._
import cc.spray.revolver.RevolverPlugin._
import coffeescript.Plugin.coffeeSettings
import coffeescript.Plugin.CoffeeKeys
import coffeescript.Plugin.CoffeeKeys._


object BuildSettings {


    val buildSettings = Defaults.defaultSettings ++ Seq( // ++ GrowlingTests.growlSettings
        organization    := "VerKnowSys",
        version         := Source.fromFile("VERSION").mkString,
        scalaVersion    := Source.fromFile("VERSION-SCALA").mkString,
        resolvers       := Resolvers.all,
        logLevel        := Level.Info,
        compileOrder    := CompileOrder.JavaThenScala,

        scalacOptions   += "-Ywarn-dead-code",
        scalacOptions   += "-Xresident",
        scalacOptions   += "-g:source",
        scalacOptions   += "-explaintypes",
        scalacOptions   += "-unchecked",
        scalacOptions   += "-deprecation",

        javacOptions    += "-g:none",
        // javacOptions     += "-encoding UTF-8",
        // javacOptions     += "-source 1.6",
        // javacOptions     += "-target 1.6",
        // javacOptions     += "-Xlint:unchecked",
        javacOptions    += "-Xlint:deprecation"

    ) ++ Tasks.all

    val coreBuildSettings = buildSettings ++ assemblySettings ++ Seq(
        test in assembly := false,

        mergeStrategy in assembly := {
            case "reference.conf" | "application.conf" | "svd.logger.properties" | "META-INF/NOTICE.txt" | "META-INF/LICENSE" | "META-INF/LICENSE.txt" | "META-INF/NOTICE" =>
                MergeStrategy.concat

            case some @ x if some.endsWith(".coffee") =>
                MergeStrategy.discard

            case "about.html" | "build.number" | "META-INF/MANIFEST.MF" | "META-INF/DEPENDENCIES" | "META-INF/INDEX.LIST" | "META-INF/BCKEY.DSA" | "META-INF/BCKEY.SF" =>
                MergeStrategy.discard

            case _ =>
                MergeStrategy.deduplicate
        }

    )
}

object Resolvers {
    val akkaRepo = "Akka Maven Repository" at "http://repo.akka.io/releases"
    val jlineRepo = "JLine Project Repository" at "http://jline.sourceforge.net/m2rep"
    val javaNet = "java.net" at "http://download.java.net/maven/2"
    val scalaTools  = "releases" at "http://scala-tools.org/repo-releases"
    val jgitRepo = "jgit-repository" at "http://download.eclipse.org/jgit/maven"
    val sonatype = "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    val guiceyfruit = "guiceyfruit repo" at "http://guiceyfruit.googlecode.com/svn/repo/releases"
    val mediavks = "Media VKS" at "http://media.verknowsys.com/maven2-repository/"
    val jboss = "JBoss Repo" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
    val repoVks = "VerKnowSys Public Repository" at "http://maven.verknowsys.com/repository"
    val repo1 = "Repo1 Maven" at "http://repo1.maven.org/maven2"

    val all = Seq(akkaRepo, jlineRepo, javaNet, scalaTools, jgitRepo, sonatype, guiceyfruit, mediavks, repoVks, jboss, repo1)
}

object Dependencies {
    val akkaVersion = "2.0.3"

    val akkaActor = "com.typesafe.akka" % "akka-actor" % akkaVersion
    val akkaRemote = "com.typesafe.akka" % "akka-remote" % akkaVersion
    val akkaTestkit = "com.typesafe.akka" % "akka-testkit" % akkaVersion

    val messadmin = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
    val jna = "net.java.dev.jna" % "jna" % "3.2.7"
    val jline = "jline" % "jline" % "0.9.9"
    val scalatest = "org.scalatest" %% "scalatest" % "1.8"
    val expect4j = "net.sourceforge.expectj" % "expectj" % "2.0.1"
    val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "2.1.0.201209190230-r" // "1.0.0.201106090707-r"
    val neodatis = "org.neodatis" % "neodatis-odb" % "1.9.24.679"
    val unfilteredFilter = "net.databinder" %% "unfiltered-filter" % "0.6.4"
    val unfilteredJetty = "net.databinder" %% "unfiltered-jetty" % "0.6.4"
    val unfilteredSpec = "net.databinder" %% "unfiltered-spec" % "0.6.4" % "test"
    val scalate = "org.fusesource.scalate" % "scalate-core" % "1.5.3"
    val scalateUtil = "org.fusesource.scalate" % "scalate-util" % "1.5.3" % "test"
    val liftJson = "net.liftweb" % "lift-json_2.9.1" % "2.4" % "compile->default"
    val liftUtil = "net.liftweb" % "lift-util_2.9.1" % "2.4" % "compile->default"

    // val javax = "javax.media" % "jai-core" % "1.1.3"
    // val javaxjmf = "javax.media" % "jmf" % "2.1.1b"
    // val smack = "jivesoftware" % "smack" % "3.0.4"
    // val smackx = "jivesoftware" % "smackx" % "3.0.4"
    // val jetty = "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
    // val jetty = "org.eclipse.jetty" % "jetty-webapp" % "7.4.1.v20110513"
    val sshd = "org.apache.sshd" % "sshd-core" % "0.7.0"
    // val slf4japi = "org.slf4j" % "slf4j-api" % "1.7.1" // WARN: api change
    val commonsio = "commons-io" % "commons-io" % "1.3.2"
    val webbit = "org.webbitserver" % "webbit" % "0.4.14"
    // val scalaz = "org.scalaz" % "scalaz-core_2.9.2" % "7.0.0-M3"

    val tzip = "de.schlichtherle" % "truezip" % "6.8.4"
    val bouncycastle = "org.bouncycastle" % "bcprov-jdk16" % "1.46"


    // val servlet = "javax.servlet" % "servlet-api" % "2.5"
    // val scalate = "org.fusesource.scalate" % "scalate-core" % "1.5.0"
}

object ServeD extends Build {


    import BuildSettings._
    import Dependencies._

    lazy val served = Project("served", file("."), settings = buildSettings).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) aggregate(

        api, cli, utils, testing, root, user, common, web
    )


    lazy val root = Project("root", file("svd.root"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jline, expect4j, sshd, webbit),
            mainClass in assembly := Some("com.verknowsys.served.rootboot")
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, common, utils, web, testing % "test")


    lazy val user = Project("user", file("svd.user"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jline, jgit)
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, common, utils, web, testing % "test")


    lazy val common = Project("common", file("svd.common"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(neodatis, expect4j, bouncycastle)
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, utils, testing % "test")


    lazy val api = Project("api", file("svd.api"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(akkaRemote)
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)


    lazy val cli = Project("cli", file("svd.cli"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(jline)
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, utils, testing % "test")


    lazy val utils = Project("utils", file("svd.utils"),
        settings = buildSettings ++ Seq(
            compileOrder        := CompileOrder.Mixed,
            libraryDependencies ++= Seq(messadmin, jna, tzip, bouncycastle, sshd, liftUtil) // slf4japi/
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, testing % "test")


    lazy val web = Project("web", file("svd.web"),
        settings = buildSettings ++ coffeeSettings ++ Revolver.settings ++ Seq(
                (resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "js"),
                libraryDependencies ++= Seq(
                    unfilteredFilter, unfilteredJetty, scalate, scalateUtil, liftJson
                )
            )
        ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api, common, utils, testing % "test")

    // lazy val web = Project("web", file("svd.web"),
    //     settings = buildSettings ++ webSettings ++ Seq(
    //         libraryDependencies ++= Seq(
    //             scalate, servlet, jetty
    //         )
    //     ) //++ CoffeeScript.coffeeSettings
    // ) dependsOn(utils, testing % "test")


    lazy val testing = Project("testkit", file("svd.testing"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(scalatest, akkaTestkit, commonsio, bouncycastle)
        )
    ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) dependsOn(api)
}
