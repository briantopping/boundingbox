package boundingbox

import java.io.BufferedReader

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.collection.JavaConverters._
import scala.collection.mutable

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

    def source(in: BufferedReader): Source[Point, NotUsed] = {
        val points = mutable.Queue.empty[Point]
        var line   = 1
        Source.unfold[Iterator[String], Point](in.lines().iterator.asScala) { lines =>
            if (points.isEmpty) {
                if (!lines.hasNext)
                    None
                else {
                    points.enqueue(readLine(lines.next(), line): _*)
                    line += 1
                    Some((lines, points.dequeue()))
                }
            } else {
                Some((lines, points.dequeue()))
            }
        }
    }
}
