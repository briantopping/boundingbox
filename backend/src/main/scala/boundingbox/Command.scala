package boundingbox

sealed trait Command

case class AddPoint(p: Point) extends Command
case class Emit() extends Command
