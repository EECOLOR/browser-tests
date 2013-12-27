package org.qirx.browserTests.server

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.Props
import akka.io.IO
import akka.io.Tcp
import akka.util.Timeout

import spray.can.Http

class Server(
  val host: String, val port: Int,
  serviceFactory: Props, idleTimeout: FiniteDuration, configName: String = "custom") {

  val config = ConfigFactory.load()
  val system = ActorSystem(configName + "-system", config.getConfig(configName).withFallback(config))

  val server = system.actorOf(Props(
    new ServerActor(serviceFactory, Timeout(idleTimeout), configName)),
    configName + "-server")

  def start(): Future[Server.Stopped] = {

    val closed = Promise[Server.Stopped]

    server ! Server.Start(host, port, closed.success)

    implicit val ec = system.dispatcher

    closed.future.map { _ =>
      system.shutdown()
      system.awaitTermination()
    }
  }

  def stop(): Unit = if (!system.isTerminated) server ! Server.Stop
}

object Server {
  case class Start(host: String, port: Int, closedHandler: StopHandler)
  case object Stop

  type Stopped = Any
  type StopHandler = Any => Unit
}

class ServerActor(serviceFactory: Props, idleTimeout: Timeout, configName: String) extends Actor {
  import context.system
  import context.dispatcher

  val service = context.actorOf(serviceFactory, configName + "-service")

  lazy val receive = stopped orElse unknown

  // store with a capital in order to do pattern matching
  val IdleTimeout = idleTimeout
  val timeout = idleTimeout.duration

  var stopHandler: Server.StopHandler = null
  var scheduledTimeout: Option[Cancellable] = None

  val stopped: Receive = {
    case Server.Start(host, port, handler) =>
      stopHandler = handler
      IO(Http) ! Http.Bind(self, host, port)
      become(starting)
  }

  def starting: Receive = {
    case Http.Bound(address) =>
      scheduleTimeout()
      become(started(listener = sender))
  }

  def started(listener: ActorRef): Receive = {
    case _: Tcp.Connected =>
      sender ! Tcp.Register(self)

    case Server.Stop =>
      listener ! Http.Unbind
      become(stopping)

    case IdleTimeout =>
      self ! Server.Stop

    case message =>
      scheduleTimeout()
      service forward message
  }

  def stopping: Receive = {
    case Http.Unbound =>
      IO(Http) ! Http.CloseAll

    case Http.ClosedAll =>
      stopHandler()
      stopHandler = null
      become(stopped)
  }

  val unknown: Receive = {
    case unknown =>
      println("=================================")
      println("ServerActor got unknown message: " + unknown)
      println("=================================")
  }

  def become(receive: Receive) = context.become(receive orElse unknown)

  def scheduleTimeout() = {
    scheduledTimeout.foreach(_.cancel())
    scheduledTimeout =
      Some(system.scheduler.scheduleOnce(timeout, self, IdleTimeout))
  }
}