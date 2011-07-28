resolvers ++= Seq(
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "lessis.me repo" at "http://repo.lessis.me",
  Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.1.0-"+v))

libraryDependencies += "org.jcoffeescript" % "jcoffeescript" % "1.1" from "http://cloud.github.com/downloads/yeungda/jcoffeescript/jcoffeescript-1.1.jar"

// libraryDependencies <+= sbtVersion(v => "me.lessis" %% "coffeescripted-sbt" % "0.1.2-%s".format(v))

libraryDependencies += "me.lessis" % "coffeescripted-sbt" % "0.1.2-0.10.1" from "http://repo.lessis.me/me/lessis/coffeescripted-sbt_2.8.1/0.1.2-0.10.1/coffeescripted-sbt_2.8.1-0.1.2-0.10.1.jar"
