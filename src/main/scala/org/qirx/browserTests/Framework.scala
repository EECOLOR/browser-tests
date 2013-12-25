package org.qirx.browserTests

import sbt.testing
import org.qirx.browserTests.runner.Runner

class Framework extends testing.Framework {

  val name = "Browser tests - sbt test framework"

  val fingerprints = Array[testing.Fingerprint](
    new testing.AnnotatedFingerprint {
      val isModule = false
      val annotationName = "browser-tests"
    })

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): testing.Runner =
    new Runner(args, remoteArgs, testClassLoader)

}