package boundingbox

import java.io.{BufferedReader, StringReader}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKitBase
import org.scalatest.{FlatSpec, Matchers}

class ReaderTest extends FlatSpec with Matchers with TestKitBase {
    override implicit lazy val system: ActorSystem = ActorSystem("MySpec")

    behavior of "Line reader"
    it should "read a set of points in" in {
        Reader.readLine("-*--**--***-", 1) shouldEqual
            Seq(Point(2, 1), Point(5, 1), Point(6, 1), Point(9, 1), Point(10, 1), Point(11, 1))
    }

    behavior of "Source generator"
    it should "create a Source for points" in {
        implicit val mat = ActorMaterializer()
        val source = Reader.source(new BufferedReader(new StringReader("-*--**--***-\n")))
        val s = source.runWith(TestSink.probe[Point])
        s.request(7).expectNext(Point(2, 1), Point(5, 1), Point(6, 1), Point(9, 1), Point(10, 1), Point(11, 1)).expectComplete()
    }
}
