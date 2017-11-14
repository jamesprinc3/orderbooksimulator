package trader

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import order.{Order, OrderType}
import orderbook.OrderBook

// A trader which just matches the best price (and therefore adds depth at the edge of the book).
class BestPriceRateTrader(orderBook: OrderBook, orderType: OrderType.Value, rate: Int, private var time: LocalDateTime)
  extends RateTrader(orderBook, orderType, rate) {

  private val size = 10

  override def step(newTime: LocalDateTime): Unit = {
    val tick = ChronoUnit.NANOS.between(time, newTime)/1e9
    val tradesNeeded: Int = math.floor(rate * tick).toInt

    val price = orderType match {
      case OrderType.Buy => orderBook.getBidPrice
      case OrderType.Sell => orderBook.getAskPrice
    }

    Range(0, tradesNeeded).foreach(_ => {
      orderBook.submitOrder(Order(orderType, price, size))
    })

    time = newTime
  }

  def getTime: LocalDateTime = {
    time
  }
}
