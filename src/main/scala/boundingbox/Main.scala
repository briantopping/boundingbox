package boundingbox

import java.io.InputStream

import scala.io.Source

object Reader {
    def readLine(line: String, row: Int): Seq[Point] = {
        line.zipWithIndex.foldLeft(Seq.empty[Point]) { (s, t) =>
            t._1 match {
                case '*' =>
                    s :+ Point(t._2 + 1, row)
                case _   =>
                    s
            }
        }
    }

    def read(in: InputStream): Seq[Point] = Source.fromInputStream(in).getLines().zipWithIndex.foldLeft(Seq.empty[Point]) {
        (s, args) => s ++ readLine(args._1, args._2 + 1)
    }
}

object Main {
    def process(points: Seq[Point]): Unit = {
        var accumulator = Set.empty[Set[Point]]
        points.foreach { p =>
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
        // build a list of bounding boxes
        val rects    = accumulator.map(Rect.fromPoints)
        // strip overlapping rectangles
        val stripped = rects.filter(r => !rects.exists(i => i != r && i.intersects(r)))
        stripped.foreach(r => println(s"(${r.topLeft.x}, ${r.topLeft.y})(${r.bottomRight.x}, ${r.bottomRight.y})"))
    }

    def main(args: Array[String]): Unit = {
        args.headOption match {
            case None =>
                Reader.read(System.in) match {
                    case Nil =>
                        println("input has no points")
                        System.exit(-1)
                    case points: Seq[Point] =>
                        process(points)
                }
            case _    =>
                println("Program takes no arguments")
                System.exit(-1)
        }
    }
}
