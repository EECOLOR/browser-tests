package org.qirx.browserTests.server

import org.qirx.browserTests.runner.EventProxy

trait ResourceRouteFactory {
  def apply(classLoader:ClassLoader, eventProxy:EventProxy):ResourceRoute
}