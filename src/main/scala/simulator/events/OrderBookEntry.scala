package simulator.events

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader

/**
  * @param time time this order enterred the book (UTC)
  */
case class OrderBookEntry(side: Side.Value, trader: Trader, orderId: Int, time: LocalDateTime, price: Double, size: Double) {
  override def toString: String = {
    "ORDER " + side +
    " trader: " + trader.id +
    " orderId: " +  orderId +
    " time: " + time +
    " price: " + price +
    " size: " + size
  }
}
