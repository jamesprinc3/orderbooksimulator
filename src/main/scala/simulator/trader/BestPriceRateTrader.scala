package simulator.trader

import java.time.LocalDateTime

import breeze.stats.distributions._
import simulator.Side
import simulator.order.{LimitOrder, Order}
import simulator.orderbook.OrderBook

// A simulator.trader which just matches the best price (and therefore adds depth at the edge of the book).
class BestPriceRateTrader(side: Side.Value,
                          rate: Int,
                          private var time: LocalDateTime,
                          traderParams: TraderParams)
    extends RateTrader(side, rate, traderParams) {

  private val size = 10

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = {
    time = newTime

    val eventsToSubmit = orderBooks.map(orderBook => {
      val price = side match {
        case Side.Bid  => orderBook.getAskPrice
        case Side.Ask => orderBook.getBidPrice
      }

      val interval = new LogNormal(0.12, 1.22).sample()

      (time.plusNanos((interval * 1e6).toLong), this, orderBook, LimitOrder(time.plusNanos((interval * 1e6).toLong), side, this, price, size))
    })

    eventsToSubmit
  }

  // TODO: move this to parent class, if we get some commonality
  def getTime: LocalDateTime = {
    time
  }

  override def initialStep(orderBooks: List[OrderBook]) = {List()}
}
