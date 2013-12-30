package org.qirx.browserTests.runner

import sbt.testing

class LoggerProxy(loggers: Array[testing.Logger]) extends testing.Logger {

  val ansiCodesSupported = true

  def info(message: String): Unit =
    log(_.info, message)

  def error(message: String): Unit =
    log(_.error, message)

  def debug(message: String): Unit =
    log(_.debug, message)

  def warn(message: String): Unit =
    log(_.warn, message)

  def trace(t: Throwable): Unit =
    for (logger <- loggers) { logger.trace(t) }

  def log(method: testing.Logger => (String => Unit), message: String): Unit =
    for (logger <- loggers) {
      var loggedMessage = message
      if (!logger.ansiCodesSupported) removeColors(loggedMessage)
      method(logger)(loggedMessage)
    }

  private val colorPattern = raw"\033\[\d{1, 2}m"

  private def removeColors(message: String): String =
    message.replaceAll(colorPattern, "")
}