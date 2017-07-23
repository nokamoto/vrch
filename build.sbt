val proto = Seq(
  PB.targets in Compile := Seq(
    PB.gens.java(com.trueaccord.scalapb.compiler.Version.protobufVersion) -> ((sourceManaged in Compile).value / "protobuf-java"),
    scalapb.gen(flatPackage = true, javaConversions = true, singleLineToString = true) -> ((sourceManaged in Compile).value / "protobuf-scala")
  ),
  libraryDependencies ++= Seq(
    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    "com.google.protobuf" % "protobuf-java-util" % com.trueaccord.scalapb.compiler.Version.protobufVersion,
    "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "io.netty" % "netty-tcnative-boringssl-static" % "2.0.3.Final"
  )
)

val json = Seq(libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.1")

val http = Seq(libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0")

val http4sVersion = "0.15.13a"

val http4s = Seq(libraryDependencies ++=
  Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion
  )
)

val akka = Seq(libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.3")

val websocket = Seq(
  libraryDependencies += "com.github.andyglow" %% "websocket-scala-client" % "0.2.4" exclude("org.slf4j", "slf4j-simple")
)

val firebase = Seq(libraryDependencies += "com.google.firebase" % "firebase-admin" % "5.2.0")

val storage = Seq(libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.2.1")

val logback = Seq(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3")

val scalatest = Seq(libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test")

val config = Seq(libraryDependencies += "com.typesafe" % "config" % "1.3.1")

val commons = Seq(scalaVersion := "2.11.8") ++ logback ++ scalatest

commons

lazy val serverutil = (project in file("serverutil")).settings(commons)

lazy val vr = (project in file("vr")).settings(commons, proto, config)

lazy val vrgrpc = (project in file("vrgrpc")).settings(commons, akka).dependsOn(vr, serverutil)

lazy val docomo = (project in file("docomo")).settings(commons, json)

lazy val mockdocomo = (project in file("mockdocomo")).settings(commons, http4s, json).dependsOn(docomo)

lazy val chgrpc = (project in file("chgrpc")).
  settings(commons, json, http).dependsOn(vr, docomo, serverutil % "test", mockdocomo % "test")

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
  commons, json, http, websocket, firebase, storage,
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
