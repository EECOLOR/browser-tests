package org.qirx.browserTests

import sbt.testing
import org.qirx.browserTests.runner.Runner

class Framework extends testing.Framework {

  val name = "Browser tests"

  val fingerprints = Array[testing.Fingerprint](
    Fingerprints.Annotated,
    Fingerprints.Subclass,
    Fingerprints.SubclassModule)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): testing.Runner =
    new Runner(args, remoteArgs, testClassLoader)

}

