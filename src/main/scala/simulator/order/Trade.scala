package simulator.order

import java.time.LocalDateTime

// TODO: maybe add an exchange/orderbook id here too, if we want to have a global log of transactions
case class Trade(time: LocalDateTime, buyerId: Int, buyerOrderId: Int, sellerId: Int, sellerOrderId: Int, price: Double, size: Double) {
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
