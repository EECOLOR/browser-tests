import org.qirx.browserTests.server.DefaultResourceRoute
import org.qirx.browserTests.runner.EventProxy
import scala.io.Source
import scala.Option.option2Iterable
import spray.routing.Directive.pimpApply

class EnhancedResourceRoute(classLoader: ClassLoader, eventProxy: EventProxy)
  extends DefaultResourceRoute(classLoader, eventProxy) {

  val availableWebjars = {
    val webjarList =
      Option(classLoader.getResourceAsStream("webjars.list"))
        .map(Source.fromInputStream).map(_.getLines)

    var webjars = Map.empty[String, String]

    for {
      webjarsLines <- webjarList.toSeq
      webjarLine <- webjarsLines
    } yield {
      val Seq(name, version, path) = webjarLine.split(";").toSeq
      webjars += name -> path
    }

    webjars
  }

  override lazy val route =
    pathPrefix("webjar") {
      path(Segment / Segment) { (webjarName, fileName) =>
        serveWebjar(webjarName, fileName)
      } ~
        path(Segment) { segment =>
          val (webjarName, fileName) =
            if (segment.endsWith(".js"))
              segment.dropRight(3) -> segment
            else
              segment -> (segment + ".js")

          serveWebjar(webjarName, fileName)
        }
    } ~
      super.route

  def serveWebjar(webjarName: String, fileName: String) = {
    val webjarPath = availableWebjars.get(webjarName)
    webjarPath match {
      case Some(webjarPath) =>
        val path = webjarPath + "/" + fileName

        serveResource(path)

      case None =>
        eventProxy.log.warn(s"Could not find webjar with name '$webjarName'")
        reject
    }
  }
}
