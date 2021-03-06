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

    val buildnFile = "svd.user/src/main/resources/BUILD"
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

    val buildSettings = Defaults.defaultSettings ++ Seq(
        organization    := "Versatile Knowledge Systems",
        version         := Source.fromFile("VERSION").mkString.trim + "-b%d".format(buildNumber),
        scalaVersion    := Source.fromFile("VERSION-SCALA").mkString.trim,
        resolvers       := Resolvers.all,
        logLevel        := Level.Info,
        compileOrder    := CompileOrder.JavaThenScala,
        parallelExecution := false,

        scalacOptions   += "-Xresident",
        // scalacOptions   += "-Yrepl-sync",
        scalacOptions   += "-feature",
        scalacOptions   += "-language:postfixOps",
        // scalacOptions   += "-Xno-patmat-analysis",
        scalacOptions   += "-language:reflectiveCalls",
        scalacOptions   += "-language:implicitConversions",
        // scalacOptions   += "-Ywarn-dead-code", // throws warning for fake dead code for case objects

        // scalacOptions   += "-Yinline",
        // scalacOptions   += "-Xcheck-null",
        // scalacOptions   += "–Xshow-phases",
        // scalacOptions   += "–Xexperimental",

        scalacOptions   += "-g:vars",
        // scalacOptions   += "-explaintypes",
        scalacOptions   += "-unchecked",
        scalacOptions   += "-deprecation",

        javacOptions    += "-g:none",
        javacOptions    += "-Xlint:unchecked",
        javacOptions    += "-Xlint:deprecation"

    ) ++ Tasks.all

    val coreBuildSettings = buildSettings ++ assemblySettings ++ Seq(
        test in assembly := false,

        mergeStrategy in assembly := {
            case "rootdoc.txt" | "reference.conf" | "application.conf" | "svd.logger.properties" | "META-INF/NOTICE.txt" | "META-INF/LICENSE" | "META-INF/LICENSE.txt" | "META-INF/NOTICE" | "plugin.properties" | "META-INF/eclipse.inf" =>
                MergeStrategy.concat

            case some @ x if some.endsWith(".coffee") =>
                MergeStrategy.discard

            case some @ x if some.endsWith(".less") =>
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
    val binReleases = "ScalaSbt" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"

    val all = Seq(akkaRepo, jlineRepo, javaNet, scalaTools, jgitRepo, sonatype, guiceyfruit, repoVks, jboss, repo1, binReleases)
}

object Dependencies {
    val akkaVersion = "2.1.4"

    // Scala
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
    val actors = "org.scala-lang" % "scala-actors" % "2.10.2"
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val jline = "jline" % "jline" % "0.9.9"
    val scalatest = "org.scalatest" % "scalatest_2.10" % "2.0.M5b"
    // val unfilteredSpec = "net.databinder" % "unfiltered-spec_2.10" % "0.6.4" % "test"
    val json = "org.json4s" %% "json4s-native" % "3.1.0"
    val zeromq = "org.zeromq" % "zeromq-scala-binding_2.10.2" % "0.1.0" // 2013-09-15 06:19:23 - dmilith - built by me and hosted in verknowsys maven repository.

    // Java
    val bouncycastle = "org.bouncycastle" % "bcprov-jdk16" % "1.46"
    val messadmin = "net.sourceforge.messadmin" % "MessAdmin-Core" % "4.0"
    val sshd = "org.apache.sshd" % "sshd-core" % "0.7.0"
    val jna = "net.java.dev.jna" % "jna" % "3.2.7"
    val jnaerator = "com.nativelibs4java" % "jnaerator-runtime" % "0.11-SNAPSHOT" % "compile"
    // val expect4j = "net.sourceforge.expectj" % "expectj" % "2.0.1"
    val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "2.1.0.201209190230-r" // "1.0.0.201106090707-r"
    val neodatis = "org.neodatis" % "neodatis-odb" % "1.9.24.679"
    val javaMail = "javax.mail" % "mail" % "1.4.5"
    // val quartz = "org.quartz-scheduler" % "quartz" % "2.1.6"
    val slf4japi = "org.slf4j" % "slf4j-api" % "1.6.6"
    val webbit = "org.webbitserver" % "webbit" % "0.4.14"
    val tzip = "de.schlichtherle" % "truezip" % "6.8.4"
    val jedis = "redis.clients" % "jedis" % "2.1.0"
    val smack = "org.jivesoftware" % "smack" % "3.2.2"
    val pircbot = "pircbot" % "pircbot" % "1.5.0"
    val commonsio = "commons-io" % "commons-io" % "2.1"

    // unfiltered module dependencies:
    val commonsCodec = "commons-codec" % "commons-codec" % "1.4"
    val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.2.1"
    val jetty = "org.eclipse.jetty" % "jetty-webapp" % "7.6.8.v20121106" //"8.1.7.v20120910"
    // val jettyServer = "org.eclipse.jetty" % "jetty-server" % "7.6.8.v20121106" //"8.1.7.v20120910"
    val jettyContinuations = "org.eclipse.jetty" % "jetty-continuation" % "7.6.8.v20121106" // "8.1.7.v20120910"

    // internal verknowsys svd.unfiltered but prebuilt jar for Scala 2.10 to avoid recompilation of unchanged code (compilation speedup)
    val unfiltered = "com.verknowsys" %% "unfiltered" % "0.6.4"

    // val javax = "javax.media" % "jai-core" % "1.1.3"
    // val javaxjmf = "javax.media" % "jmf" % "2.1.1b"
    // val smackx = "jivesoftware" % "smackx" % "3.0.4"
    // val scalaz = "org.scalaz" % "scalaz-core_2.9.2" % "7.0.0-M3"

}

object ServeD extends Build {


    import BuildSettings._
    import Dependencies._


    lazy val served = Project("served", file("."), settings = buildSettings ++ Seq(
            // commands ++= Seq(warmup)
        )).settings(graph.Plugin.graphSettings: _*) aggregate(

        api, utils, testing, user, web // unfiltered, cli, root, common,
    )


    lazy val api = Project("api", file("svd.api"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(jna, zeromq, json, actors, akkaActor, akkaRemote)
        )
    ).settings(graph.Plugin.graphSettings: _*)


    lazy val utils = Project("utils", file("svd.utils"),
        settings = buildSettings ++ Seq(
            compileOrder        := CompileOrder.Mixed,
            libraryDependencies ++= Seq(commonsio, messadmin, tzip, bouncycastle, sshd, slf4japi, commonsCodec, commonsFileUpload, neodatis, javaMail, jedis, smack, pircbot) // liftUtil, quartz
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, testing % "test")


    lazy val user = Project("user", file("svd.user"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jline, jgit, webbit)
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, utils, web, testing % "test") // common,


    lazy val web = Project("web", file("svd.web"),
        settings = buildSettings ++ coffeeSettings ++ lessSettings ++ Seq( // ++ Revolver.settings
                (resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "js"),
                (resourceManaged in (Compile, LessKeys.less)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "css"),
                libraryDependencies ++= Seq(
                    json, jetty, jettyContinuations, unfiltered
                )
            )
        ).settings(graph.Plugin.graphSettings: _*) dependsOn(api, utils, testing % "test") // unfiltered, common,


    lazy val testing = Project("testkit", file("svd.testing"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(scalatest, commonsio, bouncycastle, akkaTestkit) // akkaTestkit
        )
    ).settings(graph.Plugin.graphSettings: _*) dependsOn(api)


}
