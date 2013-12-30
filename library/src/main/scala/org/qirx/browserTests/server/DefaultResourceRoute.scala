package org.qirx.browserTests.server

import spray.routing.Directives
import spray.routing.directives.ContentTypeResolver
import org.qirx.browserTests.runner.EventProxy
import scala.io.Source

class DefaultResourceRoute(classLoader:ClassLoader, eventProxy:EventProxy)
extends ResourceRoute with Directives {

  def route =
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

    for (resourceStream <- possibleResourceStream) yield Source.fromInputStream(resourceStream).mkString
  }
}

object DefaultResourceRoute extends ResourceRouteFactory {
  def apply(classLoader:ClassLoader, eventProxy:EventProxy) =
    new DefaultResourceRoute(classLoader, eventProxy)
}