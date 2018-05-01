package simulator.order

import java.time.LocalDateTime

import simulator.orderbook.OrderBookEntry

case class Cancel(time: LocalDateTime, order: OrderBookEntry) {
  override def toString: String = {
    "CANCEL " + time +
      " " + order.toString
  }
}