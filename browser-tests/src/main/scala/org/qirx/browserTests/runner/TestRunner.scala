package org.qirx.browserTests.runner

import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import org.qirx.browserTests.Arguments
import org.qirx.browserTests.browser.Browser
import org.qirx.browserTests.server.Service
import org.qirx.spray.embedded._

import com.gargoylesoftware.htmlunit.BrowserVersion

class TestRunner(testClassLoader: ClassLoader, arguments: Arguments,
  server: Server)(implicit executor:ExecutionContext) {

  private val Arguments(testPage, browserVersions, idleTimeout, testTimeout) =
    arguments

  def run(eventProxy: EventProxy, testName: String): Unit =
    for (browserVersion <- browserVersions)
      run(eventProxy, browserVersion, testName)

  private def run(
    eventProxy: EventProxy,
    browserVersion: BrowserVersion,
    testName: String): Unit = {

    val (listener, host, port) = bindToServerWith(eventProxy)

    val browser = new Browser(eventProxy.log, browserVersion)

    val awaitServerUnbind = new TestRunner.Awaiter("unbind", listener.unbound, eventProxy)
    try {
      eventProxy.log.info(s"Running test '$testName' in '$browserVersion'")
      run(host, port, browser, testName)
      awaitServerUnbind(testTimeout, s"If your test is allowed to run longer, please change the '${Arguments.TEST_TIMEOUT}' argument")
    } catch {
      case t: Throwable => eventProxy.error(t)
    } finally {
      browser.quit()
      if (!listener.unbound.isCompleted) {
        listener.unbind()
        awaitServerUnbind(2.seconds, "Even after calling stop it would not unbind")
      }
    }
  }

  private def run(host:Host, port:Port, browser: Browser, testName: String): Unit = {
    val url = s"http://$host:$port/$testPage?test=$testName"
    Future(browser.get(url))
  }

  private def bindToServerWith(eventProxy: EventProxy):(Listener, Host, Port) = {
    val service = Service(testClassLoader, eventProxy)
    val host = "localhost"
    val port = Port.free
    val listener = server.bind(host, port, service)

    val awaitServerBind = new TestRunner.Awaiter("bind", listener.bound, eventProxy)

    awaitServerBind(10.seconds, "")

    (listener, host, port)
  }
}

object TestRunner {

  private class Awaiter(action: String,
    stopped: Future[Unit], eventProxy: EventProxy) {

    def apply(timeout: Duration, message: String) =
      try Await.ready(stopped, timeout)
      catch {
        case t: TimeoutException =>
          eventProxy.error(
            new RuntimeException(s"Server did not $action within $timeout. $message", t))
      }
  }
}
