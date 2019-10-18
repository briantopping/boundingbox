package boundingbox

case class Point(x: Integer, y: Integer) {
    def contiguous(p: Point): Boolean = Math.abs(x - p.x) == 0 && Math.abs(y - p.y) == 0
    def contiguous(b: Rect): Boolean = b.contiguous(this)
}
case class Rect(topLeft: Point, bottomRight: Point) {
    def ++(r: Rect): Rect = Rect(Point(Math.min(topLeft.x, r.topLeft.x), Math.min(topLeft.y, r.topLeft.y)), Point(Math.max(bottomRight.x, r.bottomRight.x), Math.max(bottomRight.y, r.bottomRight.y)))

    def contiguous(other: Point): Boolean = contiguous(Rect(other))
    def contiguous(other: Rect): Boolean = !(
        topLeft.x > other.bottomRight.x
            || topLeft.y > other.bottomRight.y
            || bottomRight.x + 1 < other.topLeft.x
            || bottomRight.y + 1 < other.topLeft.y
        )
}
object Rect {
    def apply(p: Point): Rect = Rect(p, p)
}

class Tree(var bounds: Rect, var children: Set[Tree]) {
    def print(): Unit = if (children.isEmpty) {
        println(bounds)
    } else {
        children.foreach(t => println(t.bounds))
    }

    def merge(r: Rect): Tree = {
        val c = bounds.contiguous(r)
        if (c) {
            if (children.isEmpty) {
                Tree(bounds ++ r)
            } else {
                val contig = children.filter(_.bounds.contiguous(r))
                if (contig.nonEmpty) {
                    val others = children -- contig
                    Tree(bounds ++ r, others + Tree(r ++ contig.tail.foldLeft(contig.head.bounds) { (b, t) => b ++ t.bounds }))
                } else {
                    Tree(bounds ++ r, Set(this, Tree(r)))
                }
            }
        } else {
            Tree(bounds ++ r, Set(this, Tree(r)))
        }
    }
}

object Tree {
    def apply(r: Rect): Tree = new Tree(r, Set.empty[Tree])
    def apply(r: Rect, c: Set[Tree]): Tree = new Tree(r, c)
}
