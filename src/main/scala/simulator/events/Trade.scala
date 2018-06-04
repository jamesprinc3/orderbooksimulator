package simulator.events

import java.time.LocalDateTime

import simulator.traits.Loggable

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

  override def toCsvString(): Seq[String] = {
    this.productIterator.map(p => p.toString).toSeq
  }

  override def toCsvHeader(): Seq[String] = {
    this.productIterator.map(p => Loggable.camel2Underscore(p.getClass.toString)).toSeq
  }
}
