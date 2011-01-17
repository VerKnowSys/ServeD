import sbt._


class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

    val extract = "org.scala-tools.sbt" % "installer-plugin" % "0.3.0" // sbt installer action
    val lessRepo = "lessis repo" at "http://repo.lessis.me"
    val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
    val akkaPlugin = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.0-RC3"
}
