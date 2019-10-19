name := "boundingbox"

version := "1.0"

scalaVersion := "2.11.12"
lazy val akkaVersion            = "2.5.25"
lazy val cassandraPluginVersion = "0.99"

fork in Test := true

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.thesamet" %% "kdtree" % "1.0.4",
    "com.github.scopt" %% "scopt" % "3.7.1",

    // test dependencies
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "commons-io" % "commons-io" % "2.4" % "test")
