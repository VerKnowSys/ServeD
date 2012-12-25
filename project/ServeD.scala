/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

import sbt._
import sbt.Keys._
import java.io.{File => JFile, FileWriter, PrintWriter}
import scala.io.Source
import sbtassembly.Plugin._
import AssemblyKeys._
// import cc.spray.revolver.RevolverPlugin._
import coffeescript.Plugin.coffeeSettings
import coffeescript.Plugin.CoffeeKeys
import coffeescript.Plugin.CoffeeKeys._
import net.virtualvoid.sbt._
import less.Plugin._


object BuildSettings {

    val buildnFile = "svd.common/src/main/resources/BUILD"
    val buildNumber = if (new File(buildnFile).exists) {
        val value = Source.fromFile(buildnFile).mkString.trim.toInt + 1
        val outFile = new FileWriter(buildnFile)
        val out = new PrintWriter(outFile)
        out.println(value)
        out.close
        value
    } else {
        val outFile = new FileWriter(buildnFile)
        val out = new PrintWriter(outFile)
        out.println("1")
        out.close
        1
    }

    val buildSettings = Defaults.defaultSettings ++ Seq( // ++ GrowlingTests.growlSettings
        organization    := "VerKnowSys",
        version         := Source.fromFile("VERSION").mkString + "-b%d".format(buildNumber),
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
    val jboss = "JBoss Repo" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
    val repoVks = "VerKnowSys Public Repository" at "http://maven.verknowsys.com/repository"
    val repo1 = "Repo1 Maven" at "http://repo1.maven.org/maven2"

    val all = Seq(akkaRepo, jlineRepo, javaNet, scalaTools, jgitRepo, sonatype, guiceyfruit, repoVks, jboss, repo1)
}

object Dependencies {
    val akkaVersion = "2.0.3"

    // Scala
    val akkaActor = "com.typesafe.akka" % "akka-actor" % akkaVersion
    val akkaRemote = "com.typesafe.akka" % "akka-remote" % akkaVersion
    val akkaTestkit = "com.typesafe.akka" % "akka-testkit" % akkaVersion % "test"
    val jline = "jline" % "jline" % "0.9.9"
    val scalatest = "org.scalatest" %% "scalatest" % "1.8" % "test"
    val unfilteredFilter = "net.databinder" %% "unfiltered-filter" % "0.6.4"
    val unfilteredJetty = "net.databinder" %% "unfiltered-jetty" % "0.6.4"
    val unfilteredSpec = "net.databinder" %% "unfiltered-spec" % "0.6.4" % "test"
    // val scalate = "org.fusesource.scalate" % "scalate-core" % "1.5.3"
    // val scalateUtil = "org.fusesource.scalate" % "scalate-util" % "1.5.3" % "test"
    val json = "org.json4s" %% "json4s-native" % "3.0.0"

    // Java
    val bouncycastle = "org.bouncycastle" % "bcprov-jdk16" % "1.46"
    val messadmin = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
    val sshd = "org.apache.sshd" % "sshd-core" % "0.7.0"
    val jna = "net.java.dev.jna" % "jna" % "3.2.7"
    val jnaerator = "com.nativelibs4java" % "jnaerator-runtime" % "0.11-SNAPSHOT" % "compile"
    val expect4j = "net.sourceforge.expectj" % "expectj" % "2.0.1"
    val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "2.1.0.201209190230-r" // "1.0.0.201106090707-r"
    val neodatis = "org.neodatis" % "neodatis-odb" % "1.9.24.679"
    val javaMail = "javax.mail" % "mail" % "1.4.5"
    val quartz = "org.quartz-scheduler" % "quartz" % "2.1.6"
    val slf4japi = "org.slf4j" % "slf4j-api" % "1.6.6"
    val commonsio = "commons-io" % "commons-io" % "2.1"
    val webbit = "org.webbitserver" % "webbit" % "0.4.14"
    val tzip = "de.schlichtherle" % "truezip" % "6.8.4"
    val jedis = "redis.clients" % "jedis" % "2.1.0"
    // val javax = "javax.media" % "jai-core" % "1.1.3"
    // val javaxjmf = "javax.media" % "jmf" % "2.1.1b"
    // val smack = "jivesoftware" % "smack" % "3.0.4"
    // val smackx = "jivesoftware" % "smackx" % "3.0.4"
    // val scalaz = "org.scalaz" % "scalaz-core_2.9.2" % "7.0.0-M3"

}

object ServeD extends Build {


    import BuildSettings._
    import Dependencies._


    lazy val served = Project("served", file("."), settings = buildSettings).settings(graph.Plugin.graphSettings: _*) aggregate(

        api, cli, utils, testing, root, user, common, web
    )


    lazy val root = Project("root", file("svd.root"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jline, expect4j, sshd, webbit),
            mainClass in assembly := Some("com.verknowsys.served.rootboot")
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, common, user, utils, web, testing % "test")


    lazy val user = Project("user", file("svd.user"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jline, jgit, webbit)
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, common, utils, web, testing % "test")


    lazy val common = Project("common", file("svd.common"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(neodatis, expect4j, bouncycastle, json, javaMail, unfilteredFilter, jedis, commonsio)
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, utils, testing % "test")


    lazy val api = Project("api", file("svd.api"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(akkaRemote)
        )
    ).settings(graph.Plugin.graphSettings: _*)


    lazy val cli = Project("cli", file("svd.cli"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(jline)
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, utils, testing % "test")


    lazy val utils = Project("utils", file("svd.utils"),
        settings = buildSettings ++ Seq(
            compileOrder        := CompileOrder.Mixed,
            libraryDependencies ++= Seq(messadmin, jna, tzip, bouncycastle, sshd, slf4japi, quartz) // liftUtil,
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, testing % "test")


    lazy val web = Project("web", file("svd.web"),
        settings = buildSettings ++ coffeeSettings ++ lessSettings ++ Seq( // ++ Revolver.settings
                (resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "js"),
                (resourceManaged in (Compile, LessKeys.less)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "css"),
                libraryDependencies ++= Seq(
                    unfilteredFilter, unfilteredJetty, json // scalate, scalateUtil,
                )
            )
        ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, common, utils, testing % "test")


    lazy val testing = Project("testkit", file("svd.testing"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(scalatest, akkaTestkit, commonsio, bouncycastle)
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api)


}
