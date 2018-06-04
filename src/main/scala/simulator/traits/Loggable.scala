package simulator.traits

object Loggable {
  def camel2Underscore(name: String): String = {
    name.map(c => if (c.isUpper) "_" + c.toLower else c.toString).mkString("")
  }
}

trait Loggable {

  implicit val mirror = scala.reflect.runtime.currentMirror

  def toCsvString: Seq[String] = {
    Seq()
  }

  def getCsvHeader: Seq[String] = {
    Seq()
  }
}
