package org.qirx.browserTests.browser

import com.gargoylesoftware.htmlunit.BrowserVersion
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import sbt.testing
import com.gargoylesoftware.htmlunit.WebConsole

class Browser(logger: testing.Logger, version: BrowserVersion) extends HtmlUnitDriver(version) {

  val l = java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter")
  l.setFilter(ConfigurableFilter)

  setJavascriptEnabled(true)

  getWebClient.getWebConsole.setLogger(new WebConsole.Logger {

    def trace(message: AnyRef) =
      message match {
        case t: Throwable => logger.trace(t)
        case other => logger.trace(new RuntimeException(other.toString))
      }
    def debug(message: AnyRef) = logger.debug(message.toString)
    def info(message: AnyRef) = logger.info(message.toString)
    def warn(message: AnyRef) = logger.warn(message.toString)
    def error(message: AnyRef) = logger.error(message.toString)
  })
}