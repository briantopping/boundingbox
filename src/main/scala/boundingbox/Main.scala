package boundingbox

import java.io.{File, FileInputStream, InputStream}

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, GraphDSL, MergePreferred, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

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
                var binding: Future[Http.ServerBinding] = Future.failed(new IllegalStateException("No server ever started"))

                val mat = if (config.interactive) {
                    val commandQ = Source.queue[Command](3, OverflowStrategy.fail)
                    binding = startWeb(commandQ)
                    graph(config.in, Some(commandQ))
                } else
                    graph(config.in, None)
                mat.run().andThen {
                    case Success(_) =>
                        binding.flatMap(_.unbind())
                        system.terminate()
                }

            case None =>
                System.exit(-1)
        }
    }

    private def graph(in: InputStream, commandSource: Option[Source[Command, SourceQueueWithComplete[Command]]] = None): RunnableGraph[Future[Done]] = {
        val sink = Sink.foreach[Rect](r => println(s"(${r.topLeft.x}, ${r.topLeft.y})(${r.bottomRight.x}, ${r.bottomRight.y})"))
        RunnableGraph.fromGraph(GraphDSL.create(sink) {
            implicit builder: GraphDSL.Builder[Future[Done]] =>
                sink =>
                    import akka.stream.scaladsl.GraphDSL.Implicits._
                    // for the streaming demo, we want `Point` to be wrapped in an `AddPoint` command
                    val pointStream = Reader.source(in).map[Command](AddPoint)
                    val processor   = new Processor()

                    def flow: Flow[Command, Rect, NotUsed] = {
                        Flow[Command].mapConcat {
                            case AddPoint(p) =>
                                processor.addPoint(p)
                                Set.empty[Rect]
                            case Emit()      =>
                                processor.results()
                        }
                    }

                    commandSource match {
                        case None     =>
                            // in non-interactive mode, just emit at the end of the stream
                            pointStream.concat(Source.single(Emit())) ~> flow ~> sink
                        case Some(cs) =>
                            val merge = builder.add(MergePreferred[Command](1))
                            pointStream ~> merge
                            cs ~> merge.preferred
                            merge ~> flow ~> sink
                    }
                    ClosedShape
        })
    }

    private def startWeb(commandQ: Source[Command, SourceQueueWithComplete[Command]])(implicit as: ActorSystem, mat: ActorMaterializer): Future[Http.ServerBinding] = {
        val route: Route = getFromResourceDirectory("")

        Http().bindAndHandle(route, "localhost", 8080)
    }
}
