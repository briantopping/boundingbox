name := "boundingbox"

version := "1.0"

scalaVersion := "2.11.12"
lazy val akkaVersion            = "2.5.25"

fork in Test := true

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

    // test dependencies
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "commons-io" % "commons-io" % "2.4" % "test")
