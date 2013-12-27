package org.qirx.browserTests.runner

import sbt.testing

case class EventProxy(
  handler: testing.EventHandler,
  loggers: Array[testing.Logger],
  events: Events) {

  import events._

  def error(t: Throwable): Unit =
    logWithEvent(t)(_.error, Error)

  def error(message: String, stack: Array[StackTraceElement]): Unit =
    logWithEvent(toException(message, stack))(_.error, Error)

  def failure(message: String, stack: Array[StackTraceElement]) =
    logWithEvent(toException(message, stack))(_.error, Failure)

  def succeeded(message: String) =
    logWithEvent(message)(_.info, Succeeded)

  def skipped(message: String) =
    logWithEvent(message)(_.info, Skipped)

  def pending(message: String) =
    logWithEvent(message)(_.info, Pending)

  def ignored(message: String) =
    logWithEvent(message)(_.info, Ignored)

  def canceled(message: String) =
    logWithEvent(message)(_.info, Canceled)

  val log = new LoggerProxy(loggers)

  private def toException(message: String, stackTrace: Array[StackTraceElement]): RuntimeException = {
    val e = new RuntimeException(message)
    e.setStackTrace(stackTrace)
    e
  }

  type LoggerMethod = testing.Logger => (String => Unit)

  // Double parameter list to help the compiler select a method
  private def logWithEvent(t: Throwable)(method: LoggerMethod, event: Throwable => Event): Unit =
    logWithEvent(messageWithStack(t))(method, event(t))

  private def logWithEvent(message: String)(method: LoggerMethod, event: Event): Unit = {
    handler handle event
    log.log(method, message)
  }

  private def messageWithStack(t: Throwable): String =
    t.getMessage + t.getStackTrace.mkString("\n", "\n", "")
}
