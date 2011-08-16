resolvers ++= Seq(
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "lessis.me repo" at "http://repo.lessis.me",
  Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.1.0-"+v))

libraryDependencies += "org.jcoffeescript" % "jcoffeescript" % "1.1" from "http://cloud.github.com/downloads/yeungda/jcoffeescript/jcoffeescript-1.1.jar"

libraryDependencies <+= sbtVersion(v => "me.lessis" %% "sbt-growl-plugin" % "0.1.1-%s".format(v))

libraryDependencies <+= sbtVersion(v => "me.lessis" %% "coffeescripted-sbt" % "0.1.3-%s".format(v))

libraryDependencies <+= sbtVersion(v => "com.eed3si9n" %% "sbt-assembly" % "sbt%s_0.4".format(v))
