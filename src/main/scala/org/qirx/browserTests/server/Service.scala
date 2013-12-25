package org.qirx.browserTests.server

import scala.io.Source

import org.qirx.browserTests.runner.EventProxy

import akka.actor.Props

import spray.routing.HttpServiceActor
import spray.routing.directives.ContentTypeResolver

object Service {

  def apply(classLoader: ClassLoader, eventProxy: EventProxy): Props =
    Props(new ServiceActor(classLoader, eventProxy))
}

class ServiceActor(
  classLoader: ClassLoader,
  eventProxy: EventProxy) extends HttpServiceActor {

  def receive: Receive = runRoute(route)

  val eventProxyRoutes = new EventProxyRoutes(eventProxy)

  val route =
    pathSingleSlash {
      get {
        reject
      }
    } ~
      pathPrefix("event") {
        eventProxyRoutes.eventRoutes
      } ~
      pathPrefix("log") {
        eventProxyRoutes.logRoutes
      } ~
      path("webjars" / Rest) { rest =>
        val webjarPath = "META-INF/resources/webjars/" + rest
        serveResource(webjarPath)
      } ~
      path(Rest) { path =>
        serveResource(path)
      }

  def serveResource(name: String)(implicit resolver: ContentTypeResolver) =
    get {
      respondWithMediaType(resolver(name).mediaType) {
        resourceAsString(name) match {
          case Some(resource) => complete(resource)
          case None =>
            eventProxy.log.warn(s"Could not find resource with name '$name'")
            reject
        }
      }
    }

  def resourceAsString(name: String) = {
    val possibleResourceStream = Option(classLoader.getResourceAsStream(name))

    for (resourceStream <- possibleResourceStream) yield
      Source.fromInputStream(resourceStream).mkString
  }
}
