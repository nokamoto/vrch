
val commons = Seq(scalaVersion := "2.11.8", libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test")

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
