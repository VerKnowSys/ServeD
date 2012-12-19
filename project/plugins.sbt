resolvers ++= Seq(
    Classpaths.sbtPluginReleases,
    "Web plugin repo" at "http://siasia.github.com/maven2",
    "lessis.me repo" at "http://repo.lessis.me",
    // "spray repo" at "http://repo.spray.io",
    "Typesafe repository" at "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/",
    "sbt-plugin-snapshots" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"
)

libraryDependencies += "org.jcoffeescript" % "jcoffeescript" % "1.1" from "http://cloud.github.com/downloads/yeungda/jcoffeescript/jcoffeescript-1.1.jar"


addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.4")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.10")

// addSbtPlugin("cc.spray" % "sbt-revolver" % "0.6.1")

addSbtPlugin("me.lessis" % "coffeescripted-sbt" % "0.2.3")

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("me.lessis" % "sbt-growl-plugin" % "0.1.3")

addSbtPlugin("me.lessis" % "less-sbt" % "0.1.10")