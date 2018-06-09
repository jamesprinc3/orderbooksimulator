package simulator.events

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader
import simulator.traits.Loggable

object OrderBookEntry extends Loggable {
  override def getCsvHeader: Seq[String] = {
    Seq("side", "price", "size")
  }
}

/**
  * @param time time this order enterred the book (UTC)
  */
case class OrderBookEntry(side: Side.Value, trader: Trader, orderId: Int, time: LocalDateTime, price: Double, size: Double) extends Loggable {
  override def toString: String = {
    "ORDER " + side +
    " trader: " + trader.id +
    " orderId: " +  orderId +
    " time: " + time +
    " price: " + price +
    " size: " + size
  }

  override def toCsvString: Seq[String] = {
    Seq(side.toString, price.toString, size.toString)
  }
}
