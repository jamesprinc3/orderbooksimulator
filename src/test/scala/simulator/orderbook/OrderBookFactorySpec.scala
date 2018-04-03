package simulator.orderbook

import simulator.order.{Order, OrderType}
import org.scalatest._
import simulator.TestConstants
import simulator.trader.{TestTrader, TraderParams}

class OrderBookFactorySpec extends FlatSpec {
  private val buyPrice = 11
  private val sellPrice = 9

  private val buyOrders = Range(0,5).map(_ => Order(OrderType.Buy, buyPrice, 10)).toList
  private val sellOrders = Range(0,5).map(_ => Order(OrderType.Sell, sellPrice, 10)).toList

  "getOrderBook" should "give empty OrderBook when given no orders" in {
    val orderBook = OrderBookFactory.getOrderBook()

    assert(orderBook.getNumberOfOrders == 0)
  }

  it should "give correct number of orders in OrderBook when given one buy order" in {
    val buyOrder = Order(OrderType.Buy, buyPrice, 10)
    val orderBook = OrderBookFactory.getOrderBook(List(buyOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given one sell order" in {
    val sellOrder = Order(OrderType.Sell, sellPrice, 10)
    val orderBook = OrderBookFactory.getOrderBook(List(sellOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given mutilple buy orders" in {
    val orderBook = OrderBookFactory.getOrderBook(buyOrders)

    assert(orderBook.getNumberOfOrders == 5)
  }

  it should "give correct number of orders in OrderBook when given multiple sell orders" in {
    val orderBook = OrderBookFactory.getOrderBook(sellOrders)

    assert(orderBook.getNumberOfOrders == 5)
  }

  it should "give correct number of orders in OrderBook when given mix of orders" in {
    val orderBook = OrderBookFactory.getOrderBook(buyOrders ++ sellOrders)

    assert(orderBook.getNumberOfOrders == 10)
  }


}
