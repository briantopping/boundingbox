package boundingbox

case class Point(x: Integer, y: Integer) {
    def contiguous(p: Point): Boolean =
        x - p.x == 0 && Math.abs(y - p.y) <= 1 ^ Math.abs(x - p.x) <= 1 && y - p.y == 0
}
case class Rect(topLeft: Point, bottomRight: Point) {
    def ++(r: Rect): Rect = Rect(
        Point(Math.min(topLeft.x, r.topLeft.x), Math.min(topLeft.y, r.topLeft.y)),
        Point(Math.max(bottomRight.x, r.bottomRight.x), Math.max(bottomRight.y, r.bottomRight.y))
    )

    def intersects(other: Rect): Boolean = !(
        topLeft.x > other.bottomRight.x
            || topLeft.y > other.bottomRight.y
            || bottomRight.x + 1 < other.topLeft.x
            || bottomRight.y + 1 < other.topLeft.y
        )
}
object Rect {
    def fromPoints(points: Set[Point]): Rect = points.tail.foldLeft(Rect(points.head)) {
        (z, p) => z ++ Rect(p)
    }
    def apply(p: Point): Rect = Rect(p, p)
}

