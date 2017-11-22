package orderbook

import order.{Order, OrderType}
import org.scalatest._
import trader.{TestTrader, TraderParams}

class OrderBookSpec extends FlatSpec {

  // TODO: remove this duplicate between here and OrderBookSideSpec
  val orderbook = TestOrderBook.getEmptyOrderBook
  val traderParams = TraderParams(orderbook, 0, 10, 10)
  val trader = new TestTrader(traderParams)

  // TODO: make this into some kind of system constant
  val minOrderIndex = 0

  "submitOrder" should "submit a buy order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val newPrice = orderBook.getBidPrice + 1

    val order = Order(OrderType.Buy, newPrice, 10)
    orderBook.submitOrder(null, order)

    assert(orderBook.getBidPrice == newPrice)
  }

  "submitOrder" should "submit a sell order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val newPrice = orderBook.getAskPrice - 1

    val order = Order(OrderType.Sell, newPrice, 10)
    orderBook.submitOrder(null, order)

    assert(orderBook.getAskPrice == newPrice)
  }

  "cancelOrder" should "cancel an order" in {
    val order = Order(OrderType.Sell, 10, 10)
    val orderBook = TestOrderBook.getOrderBook(List(order))

    assert(orderBook.cancelOrder(minOrderIndex))
  }
}
