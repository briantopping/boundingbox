package boundingbox

import com.thesamet.spatial.{DimensionalOrdering, KDTree, Metric, Region}

import scala.io.Source


object Main {
    implicit val dimOrdering: DimensionalOrdering[Point] =
        new DimensionalOrdering[Point] {
            override def dimensions: Int = 2
            override def compareProjection(dimension: Int)(x: Point, y: Point): Int = {
                dimension match {
                    case 0 => x.x - y.x
                    case 1 => x.y - y.y
                }
            }
        }

    implicit val metric: Metric[Point, Int] = new Metric[Point, Int] {
        /** Returns the distance between two points. */
        override def distance(x: Point, y: Point): Int = x.x * y.x + x.y * y.y

        /** Returns the distance between x and a hyperplane that passes through y and perpendicular to
         * that dimension.
         */
        override def planarDistance(dimension: Int)(x: Point, y: Point): Int = {
            dimension match {
                case 0 => x.x * y.x
                case 1 => x.y * y.y
            }
        }
    }

    def main(args: Array[String]): Unit = {
        args.headOption match {
            case None =>
                def read(line: String, row: Int): Seq[Point] = {
                    line.zipWithIndex.foldLeft(Seq.empty[Point]) { (s, t) =>
                        t._1 match {
                            case '*' =>
                                s :+ Point(t._2 + 1, row)
                            case _   =>
                                s
                        }
                    }
                }

                val points = Source.fromInputStream(System.in).getLines().zipWithIndex.foldLeft(Seq.empty[Point]) {
                    (s, args) => s ++ read(args._1, args._2 + 1)
                }

                points match {
                    case Nil =>
                        println("input has no points")
                    case head :: tail =>
                        val tree    = KDTree.fromSeq(points)
                        println(tree)
                        //                        val tl      = Point(2, 10)
//                        val br      = Point(4, 12)
//                        val build   = Region.from(tl, 0)
//                            .and(Region.from(tl, 1))
//                            .and(Region.to(br, 0))
//                            .and(Region.to(br, 1))
//                            .build
//                        val nearest = tree.regionQuery(build)
                        val point   = Point(10,2)
                        val nearest = tree.findNearest(point, 4)
                        val contiguous = nearest.filter(_.contiguous(point))
                        println(contiguous)
                        tail.foldLeft(Tree(Rect(head))) {
                            (t, p) => t.merge(Rect(p))
                        }.print()
                }
            case _    =>
                println("Program takes no arguments")
        }
    }
}
