package org.qirx.browserTests

import com.gargoylesoftware.htmlunit.BrowserVersion
import scala.concurrent.duration._
import java.util.logging
import org.qirx.browserTests.server.ResourceRoute
import org.qirx.browserTests.server.DefaultResourceRoute
import org.qirx.browserTests.server.ResourceRouteFactory
import org.qirx.browserTests.server.ResourceRouteFactory
import org.qirx.browserTests.runner.EventProxy

case class Arguments(
  testPage: String,
  browserVersions: Seq[BrowserVersion] = Seq(BrowserVersion.getDefault),
  idleTimeout: FiniteDuration = 2.second,
  testTimeout: FiniteDuration = 30.seconds,
  resourceRouteFactory: ResourceRouteFactory = DefaultResourceRoute)

object Arguments {

  val TEST_PAGE = "testPage"
  val BROWSER_VERSION = "browserVersions"
  val IDLE_TIMEOUT = "idleTimeout"
  val TEST_TIMEOUT = "testTimeout"
  val RESOURCE_ROUTE_FACTORY = "resourceRouteFactory"

  def apply(args: Seq[String], classLoader: ClassLoader): Arguments = {
    val argumentMap = args.grouped(2).toSeq.map {
      case Seq(key, value) => key -> value
      case Seq(key, _*) => throw new RuntimeException(s"Key '$key' has invalid value")
    }.toMap

    val baseArguments =
      Arguments(
        argumentMap.get(TEST_PAGE)
          .getOrElse(throw new RuntimeException(s"Could not find $TEST_PAGE in arguments")))

    (argumentMap - TEST_PAGE).foldLeft(baseArguments) { (arguments, args) =>
      args match {
        case (BROWSER_VERSION, value) =>
          arguments.copy(browserVersions = getBrowserVersions(value))
        case (IDLE_TIMEOUT, value) =>
          arguments.copy(idleTimeout = getTimeout(value))
        case (TEST_TIMEOUT, value) =>
          arguments.copy(testTimeout = getTimeout(value))
        case (RESOURCE_ROUTE_FACTORY, value) =>
          arguments.copy(resourceRouteFactory = getResourceRouteFactory(value, classLoader))
        case (unknown, _) =>
          throw new RuntimeException(s"Unknown argument '$unknown'")

      }
    }
  }

  def getBrowserVersions(value: String) =
    value.split(",").map(_.trim).map(getBrowserVersion)

  def getBrowserVersion(name: String) =
    try {
      classOf[BrowserVersion].getField(name).get(null).asInstanceOf[BrowserVersion]
    } catch {
      case e @ (_: NoSuchFieldException | _: SecurityException | _: IllegalAccessException) =>
        throw new RuntimeException(s"No browser available with the name $name, make sure it exists in the com.gargoylesoftware.htmlunit.BrowserVersion class as a static field", e)
    }

  def getTimeout(value: String): FiniteDuration = {
    val d = Duration(value)
    FiniteDuration(d.length, d.unit)
  }

  def getResourceRouteFactory(name: String, classLoader: ClassLoader) =
    try {
      classLoader.loadClass(name)
        .asSubclass(classOf[ResourceRouteFactory])
        .newInstance
    } catch {
      case e @ (_: ClassNotFoundException | _: IllegalAccessException | _: InstantiationException | _: SecurityException | _: ExceptionInInitializerError) =>
        throw new RuntimeException(s"Could not create an instance of $name, make sure it exists and is on the classpath", e)
      case e: ClassCastException =>
        throw new RuntimeException(s"$name does not extend ${classOf[ResourceRouteFactory].getName}", e)
    }
}