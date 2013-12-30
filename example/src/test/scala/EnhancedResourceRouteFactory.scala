import org.qirx.browserTests.runner.EventProxy
import org.qirx.browserTests.server.ResourceRouteFactory

class EnhancedResourceRouteFactory extends ResourceRouteFactory {
  def apply(classLoader: ClassLoader, eventProxy: EventProxy) =
    new EnhancedResourceRoute(classLoader, eventProxy)
}