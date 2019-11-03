package boundingbox

import java.io.InputStream

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.collection.mutable
import scala.io.{Source => ioSource}

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

    def source(in: InputStream): Source[Point, NotUsed] = {
        val points = mutable.Queue.empty[Point]
        var line   = 1
        Source.unfold[Iterator[String], Point](ioSource.fromInputStream(in).getLines()) { lines =>
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
