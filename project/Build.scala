import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.ReleaseKeys._
import org.qirx.sbtrelease.UpdateVersionInFiles
import sbtbuildinfo.Plugin._
import sbtrelease.ReleaseStateTransformations

object BrowserTestBuild extends Build {

  // for all projects
  val defaultSettings = Seq(
    organization := "org.qirx",
    onlyScalaSourcesIn(Compile),
    onlyScalaSourcesIn(Test))

  val containerProjectSettings = defaultSettings ++ Seq(
    publishArtifact := false,
    publish := {},
    unmanagedSourceDirectories in Compile := Seq(),
    unmanagedSourceDirectories in Test := Seq())

  val ivyCredentials = Credentials(Path.userHome / ".ivy2" / ".credentials")

  val releaseSettings = ReleasePlugin.releaseSettings ++ Seq(
    releaseProcess := releaseProcess.value.map { step =>
      // remove the check from the publishArtifacts step
      if (step == ReleaseStateTransformations.publishArtifacts)
        step.copy(check = identity)
      else step
    },
    UpdateVersionInFiles(
      file("README.md"),
      file("examples/plugin-example/project/plugins.sbt"),
      file("examples/library-example/build.sbt")),
    UpdateVersionInFiles.namePattern := "browser-tests-[^\"]+")

  val libraryRepo = Seq(
    credentials += ivyCredentials,
    publishTo <<= version(rhinoflyRepo))

  val pluginRepo = Seq(
    credentials += ivyCredentials,
    publishTo <<= version(rhinoflyPluginRepo),
    publishMavenStyle := false)

  val standardProjectSettings = defaultSettings ++ libraryRepo
  val pluginProjectSettings = defaultSettings ++ pluginRepo

  lazy val root = Project(id = "browser-tests-root", base = file("."))
    .settings(containerProjectSettings: _*)
    .settings(releaseSettings: _*)
    .aggregate(browserTestsLibrary, browserTestsPlugin)

  lazy val browserTestsLibrary =
    Project(id = "browser-tests-library", base = file("library"))
      .settings(standardProjectSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "org.scala-sbt" % "test-interface" % "1.0",
          "org.qirx" %% "embedded-spray" % "0.2",
          "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.39.0"),
        resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local")

  lazy val browserTestsPlugin =
    Project(id = "browser-tests-plugin", base = file("plugin"))
      .settings(pluginProjectSettings: _*)
      .settings(buildInfoSettings: _*)
      .settings(
        sbtPlugin := true,
        sourceGenerators in Compile <+= buildInfo,
        buildInfoObject := "LibraryBuildInfo",
        buildInfoKeys := Seq[BuildInfoKey](
          name in browserTestsLibrary,
          version in browserTestsLibrary,
          organization in browserTestsLibrary),
        buildInfoPackage := "org.qirx.browserTests.build")
      .dependsOn(browserTestsLibrary)

  def onlyScalaSourcesIn(c: Configuration) =
    unmanagedSourceDirectories in c := Seq((scalaSource in c).value)

  private def rhinoflyRepoNameAndUrl(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    ("Rhinofly Internal " + repo.capitalize + " Repository", "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }
  private def rhinoflyRepo(version: String) = {
    val (name, url) = rhinoflyRepoNameAndUrl(version)
    Some(name at url)
  }

  private def rhinoflyPluginRepo(version: String) = {
    val (name, url) = rhinoflyRepoNameAndUrl(version)
    Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
  }
}