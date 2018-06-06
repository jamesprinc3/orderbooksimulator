package simulator.events

import java.time.LocalDateTime

import simulator.traits.Loggable

object DoublePrice extends Loggable {
  override def getCsvHeader: Seq[String] = {
    Seq("time", "price")
  }
}

case class DoublePrice(time: LocalDateTime, price: Double) extends Loggable {
  override def toCsvString() = {
    Seq(time.toString, price.toString)
  }
}