package boundingbox

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

case class Point(x: Int, y: Int) {
    def contiguous(p: Point): Boolean =
        x - p.x == 0 && Math.abs(y - p.y) <= 1 ^ Math.abs(x - p.x) <= 1 && y - p.y == 0
}
case class Rect(topLeft: Point, bottomRight: Point) {
    def ++(r: Rect): Rect = Rect(
        Point(Math.min(topLeft.x, r.topLeft.x), Math.min(topLeft.y, r.topLeft.y)),
        Point(Math.max(bottomRight.x, r.bottomRight.x), Math.max(bottomRight.y, r.bottomRight.y))
    )

    def intersects(other: Rect): Boolean = (
        topLeft.x <= other.bottomRight.x
            && bottomRight.x >= other.topLeft.x
            && topLeft.y <= other.bottomRight.y
            && bottomRight.y >= other.topLeft.y
        )
}
object Rect {
    def fromPoints(points: Set[Point]): Rect = points.tail.foldLeft(Rect(points.head)) {
        (z, p) => z ++ Rect(p)
    }
    def apply(p: Point): Rect = Rect(p, p)
}

object GeometryJsonProtocol extends SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
    implicit val PointFormat = jsonFormat2(Point)
    implicit val RectFormat  = jsonFormat2(Rect.apply)
}
