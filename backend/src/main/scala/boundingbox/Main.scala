package boundingbox

import java.io.{BufferedReader, File, FileInputStream, InputStream, InputStreamReader}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext
import scala.io.StdIn
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

                val fileSource = Reader.source(new BufferedReader(new InputStreamReader(config.in))).map[Command](AddPoint).concat(Source.single(Emit()))
                val processor  = new Processor()
                if (config.interactive) {
                    val (sink, source) = MergeHub.source.mapConcat(flowLogic(processor)).toMat(BroadcastHub.sink[Rect])(Keep.both).run()
                    sink.runWith(fileSource)
                    startWeb(sink, source)
                } else {
                    val sink = Sink.foreach[Rect](r => println(s"(${r.topLeft.x}, ${r.topLeft.y})(${r.bottomRight.x}, ${r.bottomRight.y})"))
                    fileSource.mapConcat(flowLogic(processor)).toMat(sink)(Keep.right).run().andThen {
                        case Success(_) => system.terminate()
                    }
                }

            case None =>
                System.exit(-1)
        }
    }

    private def flowLogic(processor: Processor)(command: Command): Set[Rect] = {
        command match {
            case AddPoint(p) =>
                processor.addPoint(p)
                Set.empty[Rect]
            case Emit()      =>
                processor.results()
        }
    }

    private def startWeb(sink: Sink[Command, NotUsed], source: Source[Rect, NotUsed])(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Unit = {
        implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
            .withFramingRenderer(Flow[ByteString].map(bs => bs ++ ByteString("\n")))

        val route: Route = {
            pathEndOrSingleSlash {
                redirect("/index.html", MovedPermanently)
            } ~ pathPrefix("api") {
                path("rects") {
                    get {
                        import GeometryJsonProtocol._
                        complete(source)
                    }
                } ~ pathPrefix("command") {
                    post {
                        import CommandJsonProtocol._
                        entity(as[Command]) { command =>
                            Source.single(command).runWith(sink)
                            complete("accepted")
                        }
                    }
                }
            } ~ getFromResourceDirectory("webapp")
        }

        val binding = Http().bindAndHandle(route, "0.0.0.0", 8080)
        println("Server online at http://localhost:8080/\nPress RETURN to stop...")
        StdIn.readLine()
        binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }
}
