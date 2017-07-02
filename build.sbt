
val commons = Seq(scalaVersion := "2.11.8", libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test")

commons

val proto = Seq(
  PB.targets in Compile := Seq(
    PB.gens.java(com.trueaccord.scalapb.compiler.Version.protobufVersion) -> ((sourceManaged in Compile).value / "protobuf-java"),
    scalapb.gen(flatPackage = true, javaConversions = true) -> ((sourceManaged in Compile).value / "protobuf-scala")
  ),
  libraryDependencies ++= Seq(
    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    "com.google.protobuf" % "protobuf-java-util" % com.trueaccord.scalapb.compiler.Version.protobufVersion,
    "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "io.netty" % "netty-tcnative-boringssl-static" % "2.0.3.Final"
  )
)

lazy val vr = (project in file("vr")).settings(commons, proto)

lazy val vrgrpc = (project in file("vrgrpc")).settings(
  commons,
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.3"
).dependsOn(vr)

lazy val chgrpc = (project in file("chgrpc")).settings(
  commons,
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.0.0",
    "com.typesafe.play" %% "play-ws-standalone-json" % "1.0.0"
  )
).dependsOn(vr)

lazy val vrchgrpc = (project in file("vrchgrpc")).settings(
  commons,
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _ *) if ps.last == "io.netty.versions.properties" =>
      MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
).dependsOn(vr, vrgrpc, chgrpc)

lazy val slackbridge = (project in file("slackbridge")).settings(
  commons,
  libraryDependencies ++= Seq(
    "com.github.andyglow" %% "websocket-scala-client" % "0.2.4",
    "org.scalaj" %% "scalaj-http" % "2.3.0",
    "com.typesafe.play" %% "play-json" % "2.6.1"
  ),
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _ *) if ps.last == "io.netty.versions.properties" =>
      MergeStrategy.discard

    case PathList("io", "netty", "internal", "tcnative", xs @ _ *) =>
      MergeStrategy.discard

    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
).dependsOn(vr)
