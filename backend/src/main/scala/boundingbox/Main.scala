package boundingbox

import java.io.{File, FileInputStream, InputStream}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.util.Success

case class Config(interactive: Boolean = false, in: InputStream = System.in)

object Main {
    def main(args: Array[String]): Unit = {
        val parser = new scopt.OptionParser[Config]("boundingbox") {
            opt[Unit]('i', "interactive").action((_, c) =>
                c.copy(interactive = true)).text("interactive mode")
            opt[File]('f', "file").valueName("<file>").action((f, c) =>
                c.copy(in = new FileInputStream(f))).text("input file")
        }

        parser.parse(args, Config()) match {
            case Some(config) =>
                implicit val system       = ActorSystem("boundingbox")
                implicit val ec           = system.dispatcher
                implicit val materializer = ActorMaterializer()

                val source    = Reader.source(config.in).map[Command](AddPoint)
                val processor = new Processor()
                if (config.interactive) {
                    val sink: Sink[Rect, Source[Rect, NotUsed]] = BroadcastHub.sink
                    val runnable                                = source.mapConcat(flowLogic(_, processor)).toMat(sink)(Keep.right)
                    startWeb(runnable)
                } else {
                    val sink                  = Sink.foreach[Rect](r => println(s"(${r.topLeft.x}, ${r.topLeft.y})(${r.bottomRight.x}, ${r.bottomRight.y})"))
                    val terminatedSource      = source.concat(Source.single(Emit()))
                    terminatedSource.mapConcat(flowLogic(_, processor)).toMat(sink)(Keep.right).run()
                }.andThen {
                    case Success(_) => system.terminate()
                }

            case None =>
                System.exit(-1)
        }
    }

    private def flowLogic(command: Command, processor: Processor): Set[Rect] = {
        command match {
            case AddPoint(p) =>
                processor.addPoint(p)
                Set.empty[Rect]
            case Emit()      =>
                processor.results()
        }
    }

    private def startWeb(runnable: RunnableGraph[Source[Rect, NotUsed]])(implicit as: ActorSystem, mat: ActorMaterializer): Future[Http.ServerBinding] = {
        import GeometryJsonProtocol._
        implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

        val route: Route = {
            pathEndOrSingleSlash {
                redirect("/index.html", MovedPermanently)
            } ~ pathPrefix("api") {
                path("rects") {
                    get {
                        complete(runnable.run())
                    }
                }
            } ~ getFromResourceDirectory("webapp")
        }

        Http().bindAndHandle(route, "0.0.0.0", 8080)
    }
}
