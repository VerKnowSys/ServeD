
pollInterval := 300

useGpg := true

useGpgAgent := true

usePgpKeyHex("B181A15A") // GPG key ID

addCommandAlias("warmup", "; compile ; clean ; compile ; clean ; compile")
// seq(Revolver.settings: _*)

// javaOptions in Revolver.reStart += "-Xmx2g"

// mainClass in Revolver.reStart := Some("com.verknowsys.served.userbootDevel")


// (LessKeys.mini in (Compile, LessKeys.less)) := true // minify less generated code
