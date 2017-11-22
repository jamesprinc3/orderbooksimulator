package trader

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import order.{Order, OrderType}

// A trader which just matches the best price (and therefore adds depth at the edge of the book).
class BestPriceRateTrader(orderType: OrderType.Value,
                          rate: Int,
                          private var time: LocalDateTime,
                          traderParams: TraderParams)
    extends RateTrader(orderType, rate, traderParams) {

  private val size = 10

  override def step(newTime: LocalDateTime): Unit = {
    val tick = ChronoUnit.NANOS.between(time, newTime) / 1e9
    val tradesNeeded: Int = math.floor(rate * tick).toInt

    val price = orderType match {
      case OrderType.Buy  => orderBook.getBidPrice
      case OrderType.Sell => orderBook.getAskPrice
    }

    Range(0, tradesNeeded).foreach(_ => {
      orderBook.submitOrder(this, Order(orderType, price, size))
    })

    time = newTime
  }

  // TODO: move this to parent class, if we get some commonality
  def getTime: LocalDateTime = {
    time
  }
}
