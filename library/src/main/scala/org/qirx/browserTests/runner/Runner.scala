package org.qirx.browserTests.runner

import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.duration._

import org.qirx.browserTests.Arguments
import org.qirx.spray.embedded.Server

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import sbt.testing

class Runner(
  val args: Array[String], val remoteArgs: Array[String],
  testClassLoader: ClassLoader) extends testing.Runner {

  def tasks(taskDefs: Array[testing.TaskDef]): Array[testing.Task] =
    if (_done) throw new IllegalStateException("Done has already been called")
    else taskDefs.map(Task(testRunner))

  def done: String = {
    closeServer()
    serverSystem.shutdown()
    testSystem.shutdown()
    serverSystem.awaitTermination()
    testSystem.awaitTermination()
    _done = true
    ""
  }

  private lazy val arguments = Arguments(args, testClassLoader)

  lazy val config = {
    val config = ConfigFactory.load(testClassLoader)
    config.getConfig("browser-tests").withFallback(config)
  }

  private val serverSystem =
    ActorSystem("browser-tests-server-system", config, testClassLoader)

  private val server =
    new Server("browser-tests", Some(arguments.idleTimeout))(serverSystem)

  private val testSystem =
    ActorSystem("browser-tests-browser-system", config, testClassLoader)

  private val testRunner = {
    import testSystem.dispatcher
    new TestRunner(testClassLoader, arguments, server)
  }

  private def closeServer() =
    try Await.ready(server.close(2.seconds), 5.seconds)
    catch {
      case t: TimeoutException =>
        println("Failed to close the server properly within 5 seconds")
    }

  private var _done = false

}