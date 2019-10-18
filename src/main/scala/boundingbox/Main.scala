package boundingbox

import scala.io.Source

object Main {
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
                        tail.foldLeft(Tree(Rect(head))) {
                            (t, p) => t.merge(Rect(p))
                        }.print()
                }
            case _    =>
                println("Program takes no arguments")
        }
    }
}
