package org.qirx.browserTests.runner

import java.net.ServerSocket
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.qirx.browserTests.Arguments
import org.qirx.browserTests.server.Server
import org.qirx.browserTests.server.Service
import sbt.testing
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener
import java.net.URL
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.ScriptException
import java.net.MalformedURLException
import com.gargoylesoftware.htmlunit.IncorrectnessListener
import java.util.logging.Handler
import java.util.logging.Filter
import java.util.logging.LogRecord
import org.qirx.browserTests.browser.ConfigurableFilter
import com.gargoylesoftware.htmlunit.BrowserVersion
import org.qirx.browserTests.browser.Browser

class Runner(
  val args: Array[String], val remoteArgs: Array[String],
  testClassLoader: ClassLoader) extends testing.Runner {

  private lazy val Arguments(testPage, browserVersions, idleTimeout, testTimeout) =
    Arguments(args, testClassLoader)

  def tasks(taskDefs: Array[testing.TaskDef]): Array[testing.Task] =
    if (_done) throw new IllegalStateException("Done has already been called")
    else taskDefs.map(Task(browserVersions, testPage, withServerAndBrowser))

  def done: String = {
    _done = true
    ""
  }

  private var _done = false

  private def withServerAndBrowser(eventProxy: EventProxy, browserVersion: BrowserVersion)(code: (String, Int, WebDriver) => Unit): Unit = {

    val (host, port) = ("localhost", freePort)

    val server = createServer(host, port, eventProxy)
    val stopped = server.start()
    val awaitServerStop = new ServerStopAwaiter(stopped, eventProxy)

    val browser = createBrowser(eventProxy.log, browserVersion)

    try {
      code(host, port, browser)
      awaitServerStop(testTimeout,
        s"If your test is allowed to run longer, please change the '${Arguments.TEST_TIMEOUT}' argument")
    } catch {
      case t: Throwable => eventProxy.error(t)
    } finally {
      browser.quit()
      server.stop()
      awaitServerStop(2.seconds, "Even after calling stop it would not stop")
    }
  }

  private def freePort = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }

  private def createServer(host: Host, port: Port, eventProxy: EventProxy) = {
    val service = Service(testClassLoader, eventProxy)
    new Server(host, port, service, idleTimeout, "browser-tests")
  }

  private class ServerStopAwaiter(
    stopped: Future[Server.Stopped], eventProxy: EventProxy) {

    def apply(timeout: Duration, message: String) =
      try Await.ready(stopped, timeout)
      catch {
        case t: TimeoutException =>
          eventProxy.error(
            new RuntimeException(s"Server did not stop within $timeout. $message", t))
      }
  }

  private def createBrowser(logger:testing.Logger, version: BrowserVersion): WebDriver = {
    new Browser(logger, version)
  }
}