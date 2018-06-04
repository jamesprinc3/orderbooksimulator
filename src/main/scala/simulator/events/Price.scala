package simulator.events

import simulator.traits.Loggable

object Price extends Loggable {
  override def getCsvHeader: Seq[String] = {
    Seq("time", "price")
  }
}

case class Price(step: Int, price: Int) extends Loggable {
  override def toCsvString() = {
    Seq(step.toString, price.toString)
  }
}
