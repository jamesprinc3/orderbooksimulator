package orderbook

import order.{Order, OrderType}
import org.scalatest._

class OrderBookSpec extends FlatSpec {

  // TODO: need to hide the sides from view in this class, they are an implementation detail
  val askSide = new OrderBookSide(OrderBookSideType.Ask)
  val bidSide = new OrderBookSide(OrderBookSideType.Bid)
  def getEmptyOrderBook = new OrderBook(askSide, bidSide)

  "submitOrder" should "submit a buy order" in {
    val orderBook = getEmptyOrderBook

    val newPrice = orderBook.getBidPrice + 1

    val order = Order(OrderType.Buy, newPrice, 10)
    orderBook.submitOrder(order)

    assert(orderBook.getBidPrice == newPrice)
  }

  "submitOrder" should "submit a sell order" in {
    val orderBook = getEmptyOrderBook

    val newPrice = orderBook.getAskPrice - 1

    val order = Order(OrderType.Sell, newPrice, 10)
    orderBook.submitOrder(order)

    assert(orderBook.getAskPrice == newPrice)
  }

  "cancelOrder" should "cancel an order" in {
    val orderBook = getEmptyOrderBook

    val newPrice = orderBook.getAskPrice - 1

    val order = Order(OrderType.Sell, newPrice, 10)
    val orderId = orderBook.submitOrder(order)

    assert(orderBook.cancelOrder(orderId))
  }
}
