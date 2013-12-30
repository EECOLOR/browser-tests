package org.qirx.browserTests.server

import spray.routing.Route

trait ResourceRoute {
  def route:Route
}