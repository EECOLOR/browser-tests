import BrowserTestsKeys._

// Include webjar plugin settings, we use this in the EnhancedResourceRouteFactory
webjarSettings

// include the default browser tests settings
browserTestsSettings

name := "browser-tests-plugin-example"

libraryDependencies ++= Seq(
  "org.webjars" % "requirejs" % "2.1.8" % "test",
  "org.webjars" % "jasmine" % "1.3.1" % "test",
  "org.webjars" % "jquery" % "1.10.2" % "test"
)

// Tell the framework you want to use testPage.html as the testPage
testPage := "testPage.html"

// If you want to run on more than one browser by default, use this setting
// To find out which are available, see: http://htmlunit.sourceforge.net/apidocs/com/gargoylesoftware/htmlunit/BrowserVersion.html
//browserVersions := Seq("INTERNET_EXPLORER_10", "FIREFOX_17")

// In this example we make it a bit easier to load webjars
resourceRouteFactory := "EnhancedResourceRouteFactory"

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