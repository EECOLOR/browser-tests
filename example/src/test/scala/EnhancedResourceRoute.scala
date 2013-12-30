
import org.qirx.browserTests.server.ResourceRouteFactory
import org.qirx.browserTests.runner.EventProxy
import org.qirx.browserTests.server.DefaultResourceRoute

class EnhancedResourceRoute(classLoader: ClassLoader, eventProxy: EventProxy)
  extends DefaultResourceRoute(classLoader, eventProxy) {

  override lazy val route =
    path("webjars" / Rest) { rest =>
      val webjarPath = "META-INF/resources/webjars/" + rest
      serveResource(webjarPath)
    } ~
      super.route
}
