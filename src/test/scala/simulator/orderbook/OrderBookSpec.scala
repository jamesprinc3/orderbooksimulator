package simulator.orderbook

import simulator.order.{Order, OrderType}
import org.scalatest._
import simulator.TestConstants

class OrderBookSpec extends FlatSpec {

  // TODO: make this into some kind of system constant
  "initialization" should "populate OrderBookSides with the correct orders" in {
    val orderBook = TestOrderBook.getEmptyOrderBook
    val newPrice = orderBook.getBidPrice + 1

    val order = Order(OrderType.Buy, newPrice, 10)
    orderBook.submitOrder(null, order)

    assert(orderBook.getBidPrice == newPrice)
  }


  "submitOrder" should "submit a buy simulator.order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook
    val newPrice = orderBook.getBidPrice + 1

    val order = Order(OrderType.Buy, newPrice, 10)
    orderBook.submitOrder(null, order)

    assert(orderBook.getBidPrice == newPrice)
  }

  "submitOrder" should "submit a sell simulator.order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook
    val newPrice = orderBook.getAskPrice - 1

    val order = Order(OrderType.Sell, newPrice, 10)
    orderBook.submitOrder(null, order)

    assert(orderBook.getAskPrice == newPrice)
  }

  "cancelOrder" should "cancel an simulator.order" in {
    val order = Order(OrderType.Sell, 10, 10)
    val orderBook = TestOrderBook.getOrderBook(List(order))

    assert(orderBook.cancelOrder(TestConstants.minOrderIndex))
  }
}
