package org.qirx.browserTests.server

import org.qirx.browserTests.runner.EventProxy
import org.qirx.spray.embedded.Listener

import akka.actor.Props
import spray.http.StatusCodes
import spray.routing.HttpServiceActor

object Service {

  def apply(eventProxyRoutes: EventProxyRoutes, resourceRoute: ResourceRoute): Props =
    Props(new ServiceActor(eventProxyRoutes, resourceRoute))
}

class ServiceActor(
  eventProxyRoutes: EventProxyRoutes,
  resourceRoute: ResourceRoute) extends HttpServiceActor {

  def receive: Receive = runRoute(route)

  val route =
    pathSingleSlash {
      get {
        reject
      }
    } ~
      pathPrefix("event") {
        eventProxyRoutes.eventRoutes ~
          path("done") {
            post {

              complete {
                context.parent ! Listener.Unbind
                StatusCodes.NoContent
              }
            }
          }
      } ~
      pathPrefix("log") {
        eventProxyRoutes.logRoutes
      } ~
      resourceRoute.route

}
