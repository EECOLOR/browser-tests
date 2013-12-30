import org.qirx.browserTests.server.ResourceRouteFactory
import org.qirx.browserTests.runner.EventProxy

class EnhancedResourceRouteFactory extends ResourceRouteFactory {
  def apply(classLoader: ClassLoader, eventProxy: EventProxy) =
    new EnhancedResourceRoute(classLoader, eventProxy)
}