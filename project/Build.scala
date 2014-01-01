import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.ReleaseKeys._
import org.qirx.sbtrelease.UpdateVersionInFiles
import sbtbuildinfo.Plugin._

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

  val ivyCredentials = Credentials(Path.userHome / ".ivy2" / ".credentials")

  val releaseSettings = ReleasePlugin.releaseSettings

  val publishSettings = releaseSettings ++ Seq(
    publishTo <<= version(rhinoflyRepo),
    credentials += ivyCredentials)

  val pluginPublishSettings = releaseSettings ++ Seq(
    publishTo <<= version(rhinoflyPluginRepo),
    publishMavenStyle := false,
    credentials += ivyCredentials)

  val standardProjectSettings = defaultSettings ++ publishSettings
  val pluginProjectSettings = defaultSettings ++ pluginPublishSettings

  lazy val root = Project(id = "browser-tests-root", base = file("."))
    .settings(containerProjectSettings: _*)
    .aggregate(browserTestsLibrary, browserTestsPlugin)

  lazy val browserTestsLibrary =
    Project(id = "browser-tests-library", base = file("library"))
      .settings(standardProjectSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "org.scala-sbt" % "test-interface" % "1.0",
          "org.qirx" %% "embedded-spray" % "0.2",
          "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.39.0"),
        resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local",
        UpdateVersionInFiles(
          file("README.md"),
          file("examples/library-example/build.sbt")))

  lazy val browserTestsPlugin =
    Project(id = "browser-tests-plugin", base = file("plugin"))
      .settings(pluginProjectSettings: _*)
      .settings(buildInfoSettings: _*)
      .settings(
        sbtPlugin := true,
        UpdateVersionInFiles(
          file("README.md"),
          file("examples/plugin-example/project/plugins.sbt")),
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