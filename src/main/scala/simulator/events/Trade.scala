package simulator.events

import java.time.LocalDateTime

import simulator.traits.Loggable
import simulator.traits.Loggable.caseClassParamsOf

object Trade extends Loggable {
  override def getCsvHeader: Seq[String] = {
    caseClassParamsOf[Trade].keysIterator
      .map(k => Loggable.camel2Underscore(k))
      .toSeq
  }

}

// TODO: maybe add an exchange/orderbook id here too, if we want to have a global log of transactions
case class Trade(time: LocalDateTime = LocalDateTime.now(),
                 buyerId: Int = -1,
                 buyerOrderId: Int = -1,
                 sellerId: Int = -1,
                 sellerOrderId: Int = -1,
                 price: Double = -1,
                 size: Double = -1)
  extends Loggable {
  override def toString: String = {
    "TRADE " + time +
      " buyerId: " + buyerId +
      " buyerOrderId: " + buyerOrderId +
      " sellerId: " + sellerId +
      " sellerOrderId: " + sellerOrderId +
      " price: " + price +
      " size: " + size
  }

  override def toCsvString: Seq[String] = {
    this.productIterator.map(p => p.toString).toSeq
  }
}
