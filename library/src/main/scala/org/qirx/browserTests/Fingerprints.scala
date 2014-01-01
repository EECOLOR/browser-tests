package org.qirx.browserTests

import sbt.testing

object Fingerprints {

  sealed trait Fingerprint

  object Annotated extends testing.AnnotatedFingerprint with Fingerprint {
    val isModule = false
    val annotationName = "browser-tests"
  }

  class SubclassFingerprint(val isModule:Boolean) extends testing.SubclassFingerprint with Fingerprint {
    val superclassName = classOf[BrowserTest].getName
    val requireNoArgConstructor = true
  }

  object SubclassModule extends SubclassFingerprint(true)
  object Subclass extends SubclassFingerprint(false)
}