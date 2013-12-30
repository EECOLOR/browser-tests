import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.releaseSettings
import org.qirx.sbtrelease.UpdateVersionInFiles

object BrowserTestBuild extends Build {

  // for all projects
  val defaultSettings = Seq(
    organization := "org.qirx",
    onlyScalaSourcesIn(Compile),
    onlyScalaSourcesIn(Test))

  val containerProjectSettings = defaultSettings ++ Seq(
    publishArtifact := false,
    unmanagedSourceDirectories in Compile := Seq(),
    unmanagedSourceDirectories in Test := Seq())

  val publishSettings = releaseSettings ++ Seq(
    publishTo <<= version(rhinoflyRepo),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"))

  val standardProjectSettings = defaultSettings ++ publishSettings

  val exampleProjectSettings = defaultSettings ++ Seq(
    publishArtifact := false)

  lazy val root = Project(id = "browser-tests-root", base = file("."))
    .settings(containerProjectSettings: _*)
    .aggregate(browserTests, example)

  lazy val browserTests =
    Project(id = "browser-tests", base = file("browser-tests"))
      .settings(standardProjectSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "org.scala-sbt" % "test-interface" % "1.0",
          "org.qirx" %% "embedded-spray" % "0.2",
          "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.39.0"),
        resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local",
        UpdateVersionInFiles(file("README.md"), file("example/build.sbt")))

  lazy val example =
    Project(id = "browser-tests-example", base = file("example"))
      .settings(exampleProjectSettings:_*)

  private def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }

  def onlyScalaSourcesIn(c: Configuration) =
    unmanagedSourceDirectories in c := Seq((scalaSource in c).value)
}