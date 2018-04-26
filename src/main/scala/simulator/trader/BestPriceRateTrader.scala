package simulator.trader

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import simulator.order.{Order, OrderType}
import simulator.orderbook.OrderBook

import breeze.stats.distributions._

// A simulator.trader which just matches the best price (and therefore adds depth at the edge of the book).
class BestPriceRateTrader(orderType: OrderType.Value,
                          rate: Int,
                          private var time: LocalDateTime,
                          traderParams: TraderParams)
    extends RateTrader(orderType, rate, traderParams) {

  private val size = 10

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = {
    time = newTime
//    val tick = ChronoUnit.NANOS.between(time, newTime) / 1e9
//    val tradesNeeded: Int = math.floor(rate * tick).toInt

    val eventsToSubmit = orderBooks.map(orderBook => {
      val price = orderType match {
        case OrderType.Buy  => orderBook.getAskPrice
        case OrderType.Sell => orderBook.getBidPrice
      }

      val interval = new LogNormal(0.12, 1.22).sample()

      (time.plusNanos((interval * 1e6).toLong), this, orderBook, Order(orderType, price, size))
    })

    eventsToSubmit
  }

  // TODO: move this to parent class, if we get some commonality
  def getTime: LocalDateTime = {
    time
  }
}
