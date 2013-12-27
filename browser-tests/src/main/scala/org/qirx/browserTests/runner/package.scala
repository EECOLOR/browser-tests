package org.qirx.browserTests

import org.openqa.selenium.WebDriver
import com.gargoylesoftware.htmlunit.BrowserVersion

package object runner {
  type Host = String
  type Port = Int
  type RunCodeFunction = (EventProxy, BrowserVersion) => ((Host, Port, WebDriver) => Unit) => Unit
}