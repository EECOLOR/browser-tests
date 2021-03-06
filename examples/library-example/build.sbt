name := "browser-tests-library-example"

libraryDependencies ++= Seq(
  "org.webjars" % "requirejs" % "2.1.8" % "test",
  "org.webjars" % "jquery" % "1.10.2" % "test",
  "org.qirx" %% "browser-tests-library" % "0.7" % "test"
)

resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"

// Define the test framework
val testFramwork = TestFramework("org.qirx.browserTests.Framework")

// Let sbt know the framework exists
testFrameworks += testFramwork

// Tell the framework you want to use testPage.html as the testPage
testOptions in Test += Tests.Argument(testFramwork, "testPage", "testPage.html")

// Select the files that you want to pass to your test page
definedTests in Test ++= {
  val testFiles = (resources in Test).value ** "*Tests.js"
  val base = (resourceDirectory in Test).value
  testFiles.get map (file => IO.relativize(base, file).get) map toTestDefinition
}

// If you want to run on more than one browser by default, use this setting
// To find out which are available, see: http://htmlunit.sourceforge.net/apidocs/com/gargoylesoftware/htmlunit/BrowserVersion.html
testOptions in Test += Tests.Argument(testFramwork, "browserVersions", "INTERNET_EXPLORER_10,FIREFOX_17")

// In this example we make it a bit easier to load webjars
testOptions in Test += Tests.Argument(testFramwork, "resourceRouteFactory", "EnhancedResourceRouteFactory")

// You can also pass test parameters on the fly, see: http://www.scala-sbt.org/0.13.0/docs/Detailed-Topics/Testing#options
// Available options:
//  testPage <name>             The page to load in the headless browser
//  browserVersions <versions>  (optional) Comma separated list of browser versions
//  idleTimeout <duration>      (optional) The amount of time the browser waits for a
//                              request from the test page before concluding the tests
//                              are complete. Default 1 second. For more options, see:
//                              https://github.com/scala/scala/blob/v2.10.3/src/library/scala/concurrent/duration/Duration.scala#L77
//  testTimeout <duration>      (optional) The amount of time the total test may take.
//                              Default is 30 seconds.
//  resourceRouteFactory        (optional) A factory that return an object that contains
//                              the route for resource loading

// Method to create a test definition from a file
// once we have an sbt plugin we can hide this
def toTestDefinition(location:String) =
  new TestDefinition(name = location,
    fingerprint =
      new sbt.testing.AnnotatedFingerprint {
        val isModule = false
        val annotationName = "browser-tests"
      },
    explicitlySpecified = false, selectors = Array.empty)