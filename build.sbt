enablePlugins(SbtTwirl)

scalaSource in Compile := baseDirectory.value / "app"

sourceDirectories in (Compile, TwirlKeys.compileTemplates) := Seq(baseDirectory.value / "app")

resourceDirectory in Compile := baseDirectory.value / "app"

scalaVersion := "2.13.1"

cancelable in Global := true

libraryDependencies := Seq(
  "com.typesafe.play" %% "play-netty-server" % "2.7.3",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
)

enablePlugins(JavaAppPackaging)
