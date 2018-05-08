package simulator.events

import java.time.LocalDateTime

case class Cancel(time: LocalDateTime, order: OrderBookEntry) {
  override def toString: String = {
    "CANCEL " + time +
      " " + order.toString
  }
}