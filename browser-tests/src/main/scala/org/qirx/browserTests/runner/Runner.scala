package org.qirx.browserTests.runner

import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import org.openqa.selenium.WebDriver
import org.qirx.browserTests.Arguments
import org.qirx.browserTests.browser.Browser
import org.qirx.browserTests.server.Service
import org.qirx.spray.embedded.Host
import org.qirx.spray.embedded.Port
import org.qirx.spray.embedded.Server
import com.gargoylesoftware.htmlunit.BrowserVersion
import akka.actor.ActorSystem
import sbt.testing
import com.typesafe.config.ConfigFactory
import scala.io.Source

class Runner(
  val args: Array[String], val remoteArgs: Array[String],
  testClassLoader: ClassLoader) extends testing.Runner {

  private lazy val arguments = Arguments(args, testClassLoader)

  lazy val config = {
    val config = ConfigFactory.load(testClassLoader)
    config.getConfig("browser-tests").withFallback(config)
  }

  private val serverSystem =
    ActorSystem("browser-tests-server-system", config, testClassLoader)

  private val server =
    new Server("browser-tests", Some(arguments.idleTimeout))(serverSystem)

  private val browserSystem =
    ActorSystem("browser-tests-browser-system", config, testClassLoader)

  private val testRunner =
    new TestRunner(testClassLoader, arguments, server, browserSystem)

  def tasks(taskDefs: Array[testing.TaskDef]): Array[testing.Task] =
    if (_done) throw new IllegalStateException("Done has already been called")
    else taskDefs.map(Task(testRunner))

  def done: String = {
    try Await.ready(server.close(2.seconds), 5.seconds)
    catch {
      case t: TimeoutException =>
        println("Failed to close the server properly within 5 seconds")
    }
    serverSystem.shutdown()
    browserSystem.shutdown()
    serverSystem.awaitTermination()
    browserSystem.awaitTermination()
    _done = true
    ""
  }

  private var _done = false

}