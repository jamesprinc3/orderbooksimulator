package simulator.events

import java.time.LocalDateTime

import simulator.traits.Loggable

object Trade extends Loggable

// TODO: maybe add an exchange/orderbook id here too, if we want to have a global log of transactions
case class Trade(time: LocalDateTime, buyerId: Int, buyerOrderId: Int, sellerId: Int, sellerOrderId: Int, price: Double, size: Double) extends Loggable {
  override def toString: String = {
    "TRADE " + time +
    " buyerId: " + buyerId +
    " buyerOrderId: " + buyerOrderId +
    " sellerId: " + sellerId +
    " sellerOrderId: " + sellerOrderId +
    " price: " + price +
    " size: " + size
  }
}
