package simulator.events

import java.time.LocalDateTime

import simulator.traits.Loggable

object Spread extends Loggable {
  override def getCsvHeader: Seq[String] = {
    Seq("time", "spread")
  }
}

case class Spread(time: LocalDateTime, price: Double) extends Loggable {
  override def toCsvString() = {
    Seq(time.toString, price.toString)
  }
}