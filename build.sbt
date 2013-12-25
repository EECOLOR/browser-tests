import ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._

releaseSettings

name := "browser-tests"

organization := "org.qirx"

libraryDependencies ++= Seq(
  "org.scala-sbt" % "test-interface" % "1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "io.spray" % "spray-can" % "1.2.0",
  "io.spray" % "spray-routing" % "1.2.0",
  "io.spray" %%  "spray-json" % "1.2.5",
  "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.39.0"
)

resolvers += "spray repo" at "http://repo.spray.io"

// `insertBeforeIn` and `updateReadmeVersion` are defined in Build.scala
releaseProcess :=
  insertBeforeIn(releaseProcess.value,
    before = commitReleaseVersion,
    step = updateReadmeVersion)

publishTo <<= version(rhinoflyRepo)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := false

def rhinoflyRepo(version: String) = {
  val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
  Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}
