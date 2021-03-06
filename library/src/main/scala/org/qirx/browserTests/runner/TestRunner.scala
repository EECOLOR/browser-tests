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

import org.qirx.browserTests.server.EventProxyRoutes

class TestRunner(testClassLoader: ClassLoader, arguments: Arguments,
  server: Server)(implicit executor: ExecutionContext) {

  private val Arguments(
    testPage, browserVersions, idleTimeout, testTimeout, resourceRouteFactory) =
    arguments

  def run(eventProxy: EventProxy, testName: String, isModule:Boolean): Unit =
    for (browserVersion <- browserVersions)
      run(eventProxy, browserVersion, testName, isModule)

  private def run(
    eventProxy: EventProxy,
    browserVersion: BrowserVersion,
    testName: String, isModule:Boolean): Unit = {

    val (listener, host, port) = bindToServerWith(eventProxy)

    val browser = new Browser(eventProxy.log, browserVersion)

    val awaitServerUnbind = new TestRunner.Awaiter("unbind", listener.unbound, eventProxy)
    try {
      eventProxy.log.info(s"Running test '$testName' in '$browserVersion'")
      run(host, port, browser, testName, isModule)
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

  private def run(host: Host, port: Port, browser: Browser, testName: String, isModule:Boolean): Unit = {
    val url = s"http://$host:$port/$testPage?isModule=$isModule&test=$testName"
    Future(browser.get(url))
  }

  private def bindToServerWith(eventProxy: EventProxy): (Listener, Host, Port) = {
    val host = "localhost"
    val port = Port.free
    val service = createService(eventProxy)
    val listener = server.bind(host, port, service)

    val awaitServerBind = new TestRunner.Awaiter("bind", listener.bound, eventProxy)

    awaitServerBind(10.seconds, "")

    (listener, host, port)
  }

  private def createService(eventProxy: EventProxy) = {
    val resourceRoute = resourceRouteFactory(testClassLoader, eventProxy)
    val eventProxyRoutes = new EventProxyRoutes(eventProxy)
    Service(eventProxyRoutes, resourceRoute)
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
