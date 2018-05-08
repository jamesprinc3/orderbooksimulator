package simulator.orderbook

import org.scalatest._
import simulator.order.{Order, Side}
import simulator.trader.HandsOffTrader

class OrderBookFactorySpec extends FlatSpec {
  private val buyPrice = 9
  private val sellPrice = 11

  private val buyOrders = Range(0,5).map(_ => Order(Side.Bid, buyPrice, 10)).toList
  private val sellOrders = Range(0,5).map(_ => Order(Side.Ask, sellPrice, 10)).toList

  private def handsOffTrader = new HandsOffTrader()

  "getOrderBook" should "give empty OrderBook when given no orders" in {
    val orderBook = OrderBookFactory.getOrderBook()

    assert(orderBook.getNumberOfOrders == 0)
  }

  it should "give correct number of orders in OrderBook when given one buy order" in {
    val buyOrder = LimitOrder(Side.Bid, buyPrice, 10)
    val orderBook = OrderBookFactory.getOrderBook(List(buyOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given one sell order" in {
    val sellOrder = Order(Side.Ask, sellPrice, 10)
    val orderBook = OrderBookFactory.getOrderBook(List(sellOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given multiple buy orders" in {
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
