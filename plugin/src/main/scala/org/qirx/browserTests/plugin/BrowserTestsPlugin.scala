package org.qirx.browserTests.plugin

import sbt._
import sbt.Keys._
import org.qirx.browserTests.build.LibraryBuildInfo
import org.qirx.browserTests.Fingerprints

object BrowserTestsPlugin extends Plugin {

  object BrowserTestsKeys {
    val testPage = SettingKey[String]("test-page", "The page that is used to run the tests")
    val testNames = TaskKey[Seq[String]]("test-names", "The names that will be used to create test definitions. These will be passed to the test-page as: ?test=<testName>")
    val browserVersions = SettingKey[Seq[String]]("browser-versions", "The browser versions that you want to run the tests in. To find out which are available, see: http://htmlunit.sourceforge.net/apidocs/com/gargoylesoftware/htmlunit/BrowserVersion.html")
    val resourceRouteFactory = SettingKey[String]("resource-route-factory", "This is used by the server of the browser tests library to serve resources")
    val idleTimeout = SettingKey[String]("idle-timeout", "The amount of time the browser waits for a request from the test page before concluding the tests.")
    val testTimeout = SettingKey[String]("test-timeout", "The amount of time the total test may take.")
  }

  //testOptions in Test += Tests.Argument(testFramwork, "resourceRouteFactory", "EnhancedResourceRouteFactory")

  import BrowserTestsKeys._

  val testFramwork = TestFramework("org.qirx.browserTests.Framework")

  val library = {
    import LibraryBuildInfo._
    organization %% name % version % "test"
  }

  val browserTestsSettings = Seq(
    libraryDependencies += library,
    testFrameworks += testFramwork,
    browserVersions := Seq.empty,
    testOptions in Test ++= {
      var testOptions = Set(browserTestsArgument("testPage", testPage.value))

      val versions = browserVersions.value.mkString(",")
      if (versions.nonEmpty)
        testOptions += browserTestsArgument("browserVersions", versions)

      testOptions ++= optionalBrowserTestArgument(
        "resourceRouteFactory", resourceRouteFactory.?.value)

      testOptions ++= optionalBrowserTestArgument(
        "idleTimeout", idleTimeout.?.value)

      testOptions ++= optionalBrowserTestArgument(
        "testTimeout", testTimeout.?.value)

      testOptions.toSeq
    },
    includeFilter in testNames := "*Tests.js",
    excludeFilter in testNames := NothingFilter,
    testNames in Test := {
      val included = (includeFilter in testNames).value
      val excluded = (excludeFilter in testNames).value
      val testFiles = (resources in Test).value ** (included -- excluded)
      val base = (resourceDirectory in Test).value
      testFiles.get.map(file => IO.relativize(base, file).get)
    },
    definedTests in Test ++= {
      val names = (testNames in Test).value
      names.map(toTestDefinition)
    })

  private def browserTestsArgument(name: String, value: String) =
    Tests.Argument(testFramwork, name, value)

  def toTestDefinition(name: String) =
    new TestDefinition(name, fingerprint = Fingerprints.Annotated,
      explicitlySpecified = false, selectors = Array.empty)

  def optionalBrowserTestArgument(name: String, value: Option[String]) =
    value.map(browserTestsArgument("resourceRouteFactory", _: String))

}