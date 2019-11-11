package boundingbox

import org.scalatest.{FlatSpec, Matchers}

class ProcessorTest extends FlatSpec with Matchers {
    "A simple set" should "process correctly" in {
        val proc = new Processor()
        val points = for { x <- 2 to 3; y <- 2 to 3 } yield Point(x,y)
        points.foreach(proc.addPoint)
        proc.results() should be (Set(Rect(Point(2,2), Point(3,3))))
    }
}
