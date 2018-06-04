package simulator.traits

object Loggable {
  def camel2Underscore(name: String): String = {
    name.map(c => if (c.isUpper) "_" + c.toLower else c.toString).mkString("")
  }
}

trait Loggable {

  def toCsvString: Seq[String]

  def toCsvHeader: Seq[String]
}
