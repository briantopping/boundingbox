package boundingbox

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

sealed trait Command extends Product

case class AddPoint(p: Point) extends Command
case class Emit() extends Command

object CommandJsonProtocol extends SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
    import GeometryJsonProtocol._

    implicit val AddPointFormat = jsonFormat1(AddPoint)
    implicit val EmitFormat     = jsonFormat0(Emit)
    implicit val CommandFormat  = new RootJsonFormat[Command] {
        def write(obj: Command): JsValue = JsObject((obj match {
            case e: AddPoint => e.toJson
            case e: Emit     => e.toJson
        }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))

        def read(json: JsValue): Command = json.asJsObject.getFields("type") match {
            case Seq(JsString("AddPoint")) => json.convertTo[AddPoint]
            case Seq(JsString("Emit"))     => json.convertTo[Emit]
        }
    }
}
