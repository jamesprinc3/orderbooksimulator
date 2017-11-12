import java.time.LocalDateTime

import order.{Order, OrderType}
import orderbook.{OrderBookEntry, OrderBookSide, OrderBookSideType}
import org.scalatest._

class OrderBookSideSpec extends FlatSpec {

  def getOrderBookSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = orders.indices.map(x => {
      val order = orders(x)
      // TODO: encode this logic in just one place?
      OrderBookEntry(x, LocalDateTime.now(), order.price, order.size)
    }).toList
    new OrderBookSide(OrderBookSideType.Bid, orderBookEntries)
  }

  def emptyOrderBookSide: OrderBookSide = getOrderBookSide(List())

  val bestBuyPrice = 99
  val bestSellPrice = 101
  val standardOrderSize = 10
  val basicBuyOrder = Order(OrderType.Buy, bestBuyPrice, standardOrderSize)
  val basicSellOrder = Order(OrderType.Sell, bestSellPrice, standardOrderSize)
  def singularOrderBookSide: OrderBookSide = getOrderBookSide(List(basicBuyOrder))

  "addLimitOrder" should "add limit order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder, 0)

    assert(orderBookSide.getActiveOrders.nonEmpty)
  }

  // TODO: this test should die with generics in there
  it should "reject order of incorrect type order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicSellOrder, 0)

    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  it should "assign correct priority due to price (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    val higherPricedOrder = Order(OrderType.Buy, bestBuyPrice-1, 10)
    orderBookSide.addLimitOrder(higherPricedOrder, 0)
    orderBookSide.addLimitOrder(basicBuyOrder, 1)

    assert(orderBookSide.getActiveOrders.head.id == 1)
  }

  it should "assign correct priority due to arrival time (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder, 0)
    orderBookSide.addLimitOrder(basicBuyOrder, 1)

    assert(orderBookSide.getActiveOrders.head.id == 0)
  }

  "addMarketOrder" should "not match in an empty book" in {
    val orderBookSide = emptyOrderBookSide

    assert(orderBookSide.addMarketOrder(basicSellOrder).isDefined)
  }

  it should "match exactly one order of same size" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize)
    val ret = orderBookSide.addMarketOrder(sellOrder)

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  it should "partially match exactly one active order of same size" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize-1)
    val ret = orderBookSide.addMarketOrder(sellOrder)

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.head.size == 1)
  }

  it should "partially match exactly one incoming order" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize+1)
    val ret = orderBookSide.addMarketOrder(sellOrder)

    assert(ret.get.size == 1)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  "getBestPrice" should "display the best price with one order" in {
    assert(singularOrderBookSide.getBestPrice == bestSellPrice)
  }

  it should "display the best price with many orders of the same price" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice, 10)
    }).toList
    val orderBookSide = getOrderBookSide(orders)

    assert(orderBookSide.getBestPrice == bestPrice)
  }

  it should "display the best price with many orders of different prices" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice-x, 10)
    }).toList
    val orderBookSide = getOrderBookSide(orders)

    assert(orderBookSide.getBestPrice == bestPrice)
  }

  "cancelOrder" should "remove only active order when given a correct id" in {
    val orderBookSide = singularOrderBookSide
    orderBookSide.cancelOrder(0)

    assert(orderBookSide.getActiveOrders.toList.isEmpty)
  }

  it should "return an order from active orders when given a correct id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(0).isDefined)
  }

  it should "return a partially filled order from active orders when given a correct id" in {
    // TODO: this, requires generating orders of the other type
    true
  }

  it should "return None when given an incorrect id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(1).isEmpty)
  }
}
