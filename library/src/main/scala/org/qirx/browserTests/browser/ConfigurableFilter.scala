package org.qirx.browserTests.browser

import java.util.logging.Filter
import java.util.logging.LogRecord
import com.typesafe.config.ConfigFactory

object ConfigurableFilter extends Filter {

  private lazy val hamlessBrowserErrors = {
    val config = ConfigFactory.load().getConfig("browser-tests")
    config.getString("harmless-browser-errors")
  }

  def isLoggable(record: LogRecord) =
    !(record.getMessage matches hamlessBrowserErrors)

}