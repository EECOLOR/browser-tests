package org.qirx.browserTests.runner

import sbt.testing

class Events(taskDef: testing.TaskDef) {

  abstract class Event(
    val status: testing.Status,
    val throwable: testing.OptionalThrowable = new testing.OptionalThrowable)
    extends testing.Event {

    val fullyQualifiedName = taskDef.fullyQualifiedName
    val fingerprint = taskDef.fingerprint
    val selector = taskDef.selectors.headOption.getOrElse(new testing.SuiteSelector)
    val duration = -1L
  }

  case class Error(t: Throwable)
    extends Event(testing.Status.Error, new testing.OptionalThrowable(t))
  case class Failure(t: Throwable)
    extends Event(testing.Status.Failure, new testing.OptionalThrowable(t))
  case object Succeeded extends Event(testing.Status.Success)
  case object Skipped extends Event(testing.Status.Skipped)
  case object Pending extends Event(testing.Status.Pending)
  case object Ignored extends Event(testing.Status.Ignored)
  case object Canceled extends Event(testing.Status.Canceled)
}
