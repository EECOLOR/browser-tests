import sbt._
import sbt.Keys._
import sbtrelease.ReleaseStateTransformations.commitReleaseVersion
import sbtrelease.ReleasePlugin.releaseSettings
import sbtrelease.ReleasePlugin.ReleaseKeys.releaseProcess
import sbtrelease.ReleasePlugin.ReleaseKeys.versionControlSystem
import sbtrelease.ReleasePlugin.ReleaseKeys.releaseVersion
import sbtrelease.ReleaseStep
import sbtrelease.Vcs

object BrowserTestBuild extends Build {

  override lazy val settings = super.settings ++
    Seq(
      shellPrompt := { s => Project.extract(s).currentProject.id + " > " })

  def onlyScalaSourceIn(c: Configuration) =
    inConfig(c)(unmanagedSourceDirectories := Seq(scalaSource.value))

  val onlyScalaSource = onlyScalaSourceIn(Compile) ++ onlyScalaSourceIn(Test)

  lazy val root = Project(id = "browser-tests-root", base = file("."))
    .settings(
      publishArtifact := false,
      organization := "org.qirx")
    .aggregate(browserTests)

  lazy val browserTests =
    Project(id = "browser-tests", base = file("browser-tests"))
      .settings(releaseSettings: _*)
      .settings(onlyScalaSource: _*)
      .settings(
        libraryDependencies ++= Seq(
          "org.scala-sbt" % "test-interface" % "1.0",
          "com.typesafe.akka" %% "akka-actor" % "2.2.3",
          "io.spray" % "spray-can" % "1.2.0",
          "io.spray" % "spray-routing" % "1.2.0",
          "io.spray" %% "spray-json" % "1.2.5",
          "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.39.0"),
        resolvers += "spray repo" at "http://repo.spray.io",
        releaseProcess := withUpdatedReadme(releaseProcess.value),
        publishTo <<= version(rhinoflyRepo),
        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"))

  lazy val example =
    Project(id = "browser-tests-example", base = file("example"))
      .settings(publishArtifact := false)
      .settings(onlyScalaSource: _*)
      //TODO remove
      .dependsOn(browserTests)

  private def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }

  private def withUpdatedReadme(steps: Seq[ReleaseStep]) =
    insertBeforeIn(steps,
      before = commitReleaseVersion,
      step = updateReadmeVersion)

  private def insertBeforeIn(
    seq: Seq[ReleaseStep], before: ReleaseStep, step: ReleaseStep) = {

    val (beforeStep, rest) = seq.span(_ != before)
    (beforeStep :+ step) ++ rest
  }

  private val updateReadmeVersion: ReleaseStep = { s: State =>

    val p = Project.extract(s)
    //"org.qirx" %% "sbt-webjar" % "version"
    val pattern = "(\"" + p.get(organization) + "\"\\s+%+\\s+\"" + p.get(name) + "\"\\s+%\\s+\")[\\w\\.-]+(\")"
    val replacement = "$1" + p.get(releaseVersion)(p.get(version)) + "$2"
    val vcs = p.get(versionControlSystem).getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))

    def updateFile(fileName: String) = {
      val contents = IO.read(file(fileName))
      val newContents = contents.replaceAll(pattern, replacement)
      IO.write(file(fileName), newContents)
      vcs.add(file(fileName).getAbsolutePath) !! s.log
    }

    updateFile("README.md")
    updateFile("example/build.sbt")

    s
  }

}