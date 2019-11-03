import scala.sys.process.Process
import sbt.complete.DefaultParsers._

lazy val akkaHttpVersion  = "10.1.10"
lazy val akkaVersion      = "2.5.25"
lazy val scalaTestVersion = "3.0.8"
lazy val slf4jVersion     = "1.6.4"

lazy val npmVersion = taskKey[Unit]("Show npm version")
lazy val ngInit     = taskKey[Unit]("Inits a new Angular application, using the ui folder")
lazy val updateNpm  = taskKey[Unit]("Update npm")
lazy val ngBuild    = taskKey[Unit]("Build webapp to local directory")
lazy val npmTask    = inputKey[Unit]("Run npm with arguments")

ngBuild := {
    println("Building webapp")
    haltOnCmdResultError(Process("ng build", baseDirectory.value / "ui").!)
}

lazy val commonSettings = Seq(
    organization := "briantopping",
    name := "boundingbox",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.12.8"
)

lazy val rootSettings = Seq(
    mainClass in assembly := Some("boundingbox.Main"),
    npmVersion := {
        haltOnCmdResultError(Process("npm -version", baseDirectory.value / "ui").!)
    },
    ngInit := {
        haltOnCmdResultError(Process("npm install -g @angular/cli", baseDirectory.value).!)
        haltOnCmdResultError(Process("ng new ui --style=scss --skip-install", baseDirectory.value).!)
    },
    updateNpm := {
        println("Updating npm dependencies")
        haltOnCmdResultError(Process("npm install", baseDirectory.value / "ui").!)
    },
    npmTask := {
        val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
        updateNpm.value
        val localNpmCommand = "npm " + taskName

        def buildWebpack() =
            Process(localNpmCommand, baseDirectory.value / "ui").!

        println("Building with Webpack : " + taskName)
        haltOnCmdResultError(buildWebpack())
    }
)

def haltOnCmdResultError(result: Int) {
    if (result != 0) {
        throw new Exception("Build failed.")
    }
}

lazy val root = (project in file("."))
    .settings(rootSettings)
    .aggregate(ui, backend)

lazy val backend = (project in file("backend"))
    .settings(
        commonSettings,
        // other settings
        libraryDependencies ++= Seq(
            "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-stream" % akkaVersion,
            "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
            "com.github.scopt" %% "scopt" % "3.7.1",


            "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
            "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
            "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
            "org.scalatest" %% "scalatest" % scalaTestVersion % Test,

        )
    )

lazy val ui = (project in file("ui"))
    .settings(
        commonSettings,
        // other settings
    )

((compile in Compile) in ui) := {
    ((compile in Compile) in ui) dependsOn ngBuild
    }.value
