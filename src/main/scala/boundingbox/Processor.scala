package boundingbox

class Processor {
    var accumulator = Set.empty[Set[Point]]
    def addPoint(p: Point): Unit = {
        // for each set that contains a contiguous point
        accumulator.filter(_.exists(_.contiguous(p))) match {
            case filtered: Set[Set[Point]] if filtered.isEmpty =>
                // with no result sets, add a new set with the single point
                accumulator = accumulator + Set(p)
            case filtered: Set[Set[Point]]                     =>
                // one or more sets may be bridged by this point. Coalesce them to a single set and add
                val others = accumulator -- filtered
                accumulator = others + (filtered.flatten + p)
        }
    }
    def results(): Set[Rect] = {
        // build a list of bounding boxes
        val rects = accumulator.map(Rect.fromPoints)
        // strip and return overlapping rectangles
        rects.filter(r => !rects.exists(i => i != r && i.intersects(r)))
    }
}
