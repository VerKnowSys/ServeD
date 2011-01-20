import sbt._


class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
    val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
    
    val assemblySBT = "com.codahale" % "assembly-sbt" % "0.1.1"
    val extract = "org.scala-tools.sbt" % "installer-plugin" % "0.3.0" // sbt installer action
    val lessRepo = "lessis repo" at "http://repo.lessis.me"
    val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
    val akkaPlugin = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.0-RC3"
}
