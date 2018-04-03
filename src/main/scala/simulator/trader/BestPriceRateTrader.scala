package simulator.trader

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import simulator.order.{Order, OrderType}

// A simulator.trader which just matches the best price (and therefore adds depth at the edge of the book).
class BestPriceRateTrader(orderType: OrderType.Value,
                          rate: Int,
                          private var time: LocalDateTime,
                          traderParams: TraderParams)
    extends RateTrader(orderType, rate, traderParams) {

  private val size = 10

  override def step(newTime: LocalDateTime): Unit = {
    val tick = ChronoUnit.NANOS.between(time, newTime) / 1e9
//    val tradesNeeded: Int = math.floor(rate * tick).toInt
//    println("TradesNeeded: " + tradesNeeded)

    val price = orderType match {
      case OrderType.Buy  => orderBook.getBidPrice
      case OrderType.Sell => orderBook.getAskPrice
    }

    println("Price: " + price)

//    Range(0, tradesNeeded).foreach(_ => {
    val orderId = orderBook.submitOrder(this, Order(orderType, price, size))
    println (orderId)
//    })

    time = newTime
  }

  // TODO: move this to parent class, if we get some commonality
  def getTime: LocalDateTime = {
    time
  }
}
