import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
    val mavenVKS = "maven.verknowsys.com" at "http://maven.verknowsys.com/repository/"
    val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
    val scctRepo = "scct-repo" at "http://mtkopone.github.com/scct/maven-repo/"
    val lessRepo = "lessis repo" at "http://repo.lessis.me"
    val akkaRepo = "akka repo" at "http://akka.io/repository"
    val coffeeScriptSbtRepo = "coffeeScript sbt repo" at "http://repo.coderlukes.com"
    val retronymSnapshotRepo = "retronym's repo" at "http://retronym.github.com/repo/releases"

    val scctPlugin  = "reaktor" % "sbt-scct-for-2.8" % "0.1-SNAPSHOT"
    val onejarSBT = "com.github.retronym" % "sbt-onejar" % "0.2"
    val extract     = "org.scala-tools.sbt" % "installer-plugin" % "0.3.0" // sbt installer action
    val growl       = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
    val akkaPlugin  = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.1.3"
    val coffeeScript = "org.coffeescript" % "coffee-script-sbt-plugin" % "1.0"

    val jettyEmbeddedWarRepo = "Embeded Jetty Repo" at "https://github.com/glenford/repo/raw/master"
    val jettyEmbeddedWar = "net.usersource" % "sbt-jetty-embed-plugin" % "0.6.1"
}
