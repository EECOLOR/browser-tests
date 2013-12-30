package org.qirx.browserTests.server

import org.qirx.browserTests.runner.EventProxy

import sbt.testing
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.httpx.unmarshalling.Unmarshaller
import spray.json._
import spray.routing.Directives

class EventProxyRoutes(eventProxy: EventProxy) extends Directives {

  import EventProxyRoutes._

  val eventRoutes =
    post {
      pathEndOrSingleSlash {
        reject
      } ~
        eventWithStack("error", _.error) ~
        eventWithStack("failure", _.failure) ~
        event("succeeded", _.succeeded) ~
        event("skipped", _.skipped) ~
        event("pending", _.pending) ~
        event("ignored", _.ignored) ~
        event("canceled", _.canceled)
    }

  val logRoutes =
    post {
      pathEndOrSingleSlash {
        reject
      } ~
        log("info", _.info) ~
        log("error", _.error) ~
        log("debug", _.debug) ~
        log("warn", _.warn) ~
        pathWithMessage("trace") { m =>
          val exception = new RuntimeException(m.message)
          exception.setStackTrace(m.stack.getOrElse(Array.empty))
          eventProxy.log.trace(exception)
        }
    }

  private def eventWithStack(name: String, method: EventProxy => ((String, Array[StackTraceElement]) => Unit)) =
    pathWithMessage(name) { m =>
      method(eventProxy)(m.message, m.stack.getOrElse(Array.empty))
    }

  private def event(name: String, method: EventProxy => (String => Unit)) =
    pathWithMessage(name) { m =>
      method(eventProxy)(m.message)
    }

  private def log(name: String, method: testing.Logger => (String => Unit)) =
    pathWithMessage(name) { m =>
      method(eventProxy.log)(m.message)
    }

  private def pathWithMessage(name: String)(code: Message => Unit) =
    path(name) {
      entity(as[Message]) { message =>
        complete {
          code(message)
          StatusCodes.NoContent
        }
      }
    }
}

object EventProxyRoutes {
  case class Message(message: String, stack: Option[Array[StackTraceElement]])

  implicit object stackTraceElementReader extends JsonFormat[StackTraceElement] {
    def read(value: JsValue) = {

      val fields = value.asJsObject.fields.toMap

      type ConvertFunction[T] = PartialFunction[JsValue, Option[T]]

      def fieldAs[T](name: String)(convert: ConvertFunction[T]) = {
        val none: ConvertFunction[T] = { case _ => None }
        fields.get(name).flatMap(convert orElse none)
      }

      def fieldAsString(name: String) =
        fieldAs[String](name) {
          case JsString(value) => Some(value)
        }.getOrElse("")

      def fieldAsInt(name: String) =
        fieldAs[Int](name) {
          case JsNumber(value) => Some(value.toInt)
        }.getOrElse(-1)

      new StackTraceElement(
        fieldAsString("declaringClass"),
        fieldAsString("methodName"),
        fieldAsString("fileName"),
        fieldAsInt("lineNumber"))
    }

    /*
     * Because spray does not provide a handy jsonReader method like
     * jsonFormat, we needed to implement this class as a JsonFormat
     */
    def write(stack: StackTraceElement) = ???
  }

  import spray.json.DefaultJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  implicit val messageReader: Unmarshaller[Message] =
    jsonFormat(Message, "message", "stack")
}