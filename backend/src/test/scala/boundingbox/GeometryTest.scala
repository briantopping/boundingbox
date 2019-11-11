package boundingbox

import org.scalatest.{FlatSpec, Matchers}

class GeometryTest extends FlatSpec with Matchers {
    behavior of "Contiguous point testing"
    it should "be true for the same point" in {
        Point(1, 1).contiguous(Point(1, 1))
    }
    it should "be true for points adjscent on the X-axis" in {
        Point(1, 1).contiguous(Point(2, 1))
    }
    it should "be true for points adjacent on the Y-axis" in {
        Point(1, 1).contiguous(Point(1, 2))
    }
    it should "be false for points adjacent on both axes" in {
        Point(1, 1).contiguous(Point(2, 2))
    }
    it should "be false for distant points" in {
        Point(1, 1).contiguous(Point(3, 3))
    }

    behavior of "Rectangle maxima creation"
    it should "work for two rectangles" in {
        Rect(Point(5, 5), Point(8, 8)) ++ Rect(Point(10, 10), Point(20, 20)) shouldEqual Rect(Point(5, 5), Point(20, 20))
    }
    it should "bound a list of points" in {
        Rect.fromPoints(Set(Point(5, 5), Point(8, 12), Point(10, 10), Point(20, 20))) shouldEqual Rect(Point(5, 5), Point(20, 20))
    }

    behavior of "Rectangle conjunction testing"
    it should "be true for rectangles sharing all sides" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(5, 5), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for concentric rectangles" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(6, 6), Point(7, 7))
        ) shouldBe true
    }
    it should "be true for reversed concentric rectangles" in {
        Rect(Point(6, 6), Point(7, 7)).intersects(Rect(Point(5, 5), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for rectangles sharing a side" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(6, 6), Point(7, 8))
        ) shouldBe true
    }
    it should "be true for reversed concentric rectangles sharing a side" in {
        Rect(Point(6, 6), Point(7, 8)).intersects(Rect(Point(5, 5), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for rectangles sharing two sides" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(6, 6), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for reversed concentric rectangles sharing two sides" in {
        Rect(Point(6, 6), Point(8, 8)).intersects(Rect(Point(5, 5), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for rectangles sharing three sides" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(5, 6), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for reversed concentric rectangles sharing three sides" in {
        Rect(Point(5, 6), Point(8, 8)).intersects(Rect(Point(5, 5), Point(8, 8))
        ) shouldBe true
    }
    it should "be true for overlapping rectangles" in {
        Rect(Point(7, 8), Point(9, 9)).intersects(Rect(Point(5, 5), Point(10, 10))
        ) shouldBe true
    }
    it should "be true for non-concentric rectangles sharing a side" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(8, 7), Point(10, 10))
        ) shouldBe true
    }
    it should "be false for disjoint rectangles in one axis" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(9, 7), Point(10, 10))
        ) shouldBe false
    }
    it should "be false for disjoint rectangles in two axes" in {
        Rect(Point(5, 5), Point(8, 8)).intersects(Rect(Point(9, 9), Point(10, 10))
        ) shouldBe false
    }

    behavior of "JSON protocol"
    it should "generate JSON for a Point" in {
        import spray.json._
        import GeometryJsonProtocol._
        Point(10, 10).toJson.compactPrint shouldEqual """{"x":10,"y":10}"""
    }
    it should "parse a Point" in {
        import spray.json._
        """{"x":10,"y":10}""".parseJson equals Point(10, 10)
    }
    it should "generate JSON for a Rect" in {
        import spray.json._
        import GeometryJsonProtocol._
        Rect(Point(10, 10), Point(20, 20)).toJson.compactPrint shouldEqual """{"bottomRight":{"x":20,"y":20},"topLeft":{"x":10,"y":10}}"""
    }
    it should "parse a Rect" in {
        import spray.json._
        """{"bottomRight":{"x":20,"y":20},"topLeft":{"x":10,"y":10}}""".parseJson equals Rect(Point(10, 10), Point(20, 20))
    }
}
