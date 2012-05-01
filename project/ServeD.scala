import sbt._
import sbt.Keys._

import scala.io.Source
import com.github.siasia.WebPlugin
import coffeescript.CoffeeScript
import sbtassembly.Plugin._


object BuildSettings {
    import growl._
    import growl.GrowlingTests._

    val buildSettings = Defaults.defaultSettings ++ GrowlingTests.growlSettings ++ Seq(
        organization    := "VerKnowSys",
        version         := Source.fromFile("VERSION").mkString,
        scalaVersion    := "2.9.1",
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
        javacOptions    += "-Xlint:deprecation",

        // Customized GrowlingTests configuration
        images in Growl := GrowlTestImages(
            Some("project/growl_images/pass.png"),
            Some("project/growl_images/fail.png"),
            Some("project/growl_images/fail.png")
        ),
        groupFormatter in Growl <<= (images in Growl) {
            (imgs) =>
                (res: GroupResult) =>
                    GrowlResultFormat(
                        Some(res.name),
                        res.name.replace("com.verknowsys.served", "svd"), // shorten class name
                        res.status match {
                            case TestResult.Error  => "Had Errors"
                            case TestResult.Passed => "Passed"
                            case TestResult.Failed => "Failed"
                        },
                        res.status match {
                            case TestResult.Error | TestResult.Failed => true
                            case _ => false
                        },
                        res.status match {
                            case TestResult.Error  => imgs.error
                            case TestResult.Passed => imgs.pass
                            case TestResult.Failed => imgs.fail
                        }
                    )
        }
    ) ++ Tasks.all

    val coreBuildSettings = buildSettings ++ assemblySettings ++ Seq(
        test in Assembly := false,
        excludedFiles in Assembly := { (bases: Seq[File]) =>
            val ext = Set(".class", ".dylib", ".jnilib", ".so", ".properties", ".conf", "VERSION")

            bases flatMap { base =>
                (base ** "*").get filterNot { f => ext exists (f.getName endsWith _ ) }
            }
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
    val repoVks = "VerKnowSys Public Repository" at "http://maven.verknowsys.com/repository"

    val all = Seq(akkaRepo, jlineRepo, javaNet, scalaTools, jgitRepo, sonatype, guiceyfruit, mediavks, repoVks)
}

object Dependencies {
    val akkaVersion = "1.3.1"

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
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.9"
    val h2 = "com.h2database" % "h2" % "1.3.154"
    // val jetty = "org.eclipse.jetty" % "jetty-webapp" % "7.4.1.v20110513"
    // val funlet = "com.verknowsys" %% "funlet" % "0.1.0-SNAPSHOT"
    val sshd = "org.apache.sshd" % "sshd-core" % "0.5.0"
    val slf4japi = "org.slf4j" % "slf4j-api" % "1.5.8"
    val commonsio = "commons-io" % "commons-io" % "1.3.2"
    val webbit = "org.webbitserver" % "webbit" % "0.3.8"
}

object ServeD extends Build {
    import BuildSettings._
    import Dependencies._

    lazy val served = Project("ServeD", file("."), settings = buildSettings) aggregate(
        api, cli, utils, testing, root, user // web,
    )


    lazy val root = Project("root", file("svd.root"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(h2, expect4j, sshd, webbit),
            mainClass in Assembly := Some("com.verknowsys.served.rootboot")
        )
    ) dependsOn(common, testing % "test")


    lazy val user = Project("user", file("svd.user"),
        settings = coreBuildSettings ++ Seq(
            parallelExecution in Test := false, // NOTE: This should be removed
            libraryDependencies ++= Seq(jgit, smack)
        )
    ) dependsOn(common, testing % "test")


    lazy val common = Project("common", file("svd.common"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(neodatis, expect4j)
        )
    ) dependsOn(utils, testing % "test")


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
            compileOrder        := CompileOrder.Mixed,
            libraryDependencies ++= Seq(messadmin, jna, slf4japi)
        )
    ) dependsOn(api, testing % "test")


    // lazy val web = Project("web", file("svd.web"),
    //     settings = buildSettings ++ WebPlugin.webSettings ++ Seq(
    //         libraryDependencies ++= Seq(
    //             funlet, jetty % "jetty"
    //         )
    //     ) ++ CoffeeScript.coffeeSettings
    // ) dependsOn(utils, testing % "test")
    // 

    lazy val testing = Project("testkit", file("svd.testing"),
        settings = buildSettings ++ Seq(
            libraryDependencies ++= Seq(specs, scalatest, akkaTestkit, commonsio)
        )
    )
}
