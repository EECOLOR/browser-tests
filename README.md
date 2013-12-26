Sbt test framework that runs tests in a browser
===============================================

This library provides an sbt compatible test framework that allows you to
run tests in a browser.

Installation
------------

``` scala
val appDependencies = Seq(
  "org.qirx" %% "browser-tests" % "0.1"
)

resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
```

