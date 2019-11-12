package boundingbox

import org.scalatest.{FlatSpec, FunSuite, Matchers}

class CommandJsonProtocolTest extends FlatSpec with Matchers {
    behavior of "Command JSON parsing"
    it should "generate JSON for an AddPoint Command" in {
        import spray.json._
        import CommandJsonProtocol._
        AddPoint(Point(10, 10)).toJson.compactPrint shouldEqual """{"p":{"x":10,"y":10}}"""
    }
    it should "parse an AddPoint" in {
        import spray.json._
        """{"p":{"x":10,"y":10}}""".parseJson equals AddPoint(Point(10, 10))
    }
    it should "generate JSON for an abstract Emit Command" in {
        import spray.json._
        import CommandJsonProtocol._
        Emit().asInstanceOf[Command].toJson.compactPrint shouldEqual """{"type":"Emit"}"""
    }
    it should "parse an abstract Emit" in {
        import spray.json._
        """{"type":"Emit"}""".parseJson equals Emit()
    }
    it should "generate JSON for an abstract AddPoint Command" in {
        import spray.json._
        import CommandJsonProtocol._
        AddPoint(Point(10, 10)).asInstanceOf[Command].toJson.compactPrint shouldEqual """{"p":{"x":10,"y":10},"type":"AddPoint"}"""
    }
    it should "parse an abstract AddPoint" in {
        import spray.json._
        """{"p":{"x":10,"y":10},"type":"AddPoint"}""".parseJson equals AddPoint(Point(10, 10))
    }
}
