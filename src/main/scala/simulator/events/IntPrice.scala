package simulator.events

import simulator.traits.Loggable

object IntPrice extends Loggable

case class IntPrice(step: Int, price: Int) extends Loggable {
  override def toCsvString() = {
    Seq(step.toString, price.toString)
  }
}
