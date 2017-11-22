package simulator.orderbook


import simulator.order.{Order, OrderType}
import org.scalatest._
import simulator.trader.{TestTrader, TraderParams}

class OrderBookSideSpec extends FlatSpec {

  // TODO: sort this dependency mess out
  def emptyOrderBookSide: OrderBookSide = OrderBookSideHelper.getBidSide(List())
  val orderbook = TestOrderBook.getEmptyOrderBook
  val traderParams = TraderParams(orderbook, 0, 10, 10)
  val trader = new TestTrader(traderParams)

  val bestBuyPrice = 99
  val bestSellPrice = 101
  val standardOrderSize = 10
  val basicBuyOrder = Order(OrderType.Buy, bestBuyPrice, standardOrderSize)
  val basicSellOrder = Order(OrderType.Sell, bestSellPrice, standardOrderSize)
  def singularOrderBookSide: OrderBookSide = OrderBookSideHelper.getBidSide(List(basicBuyOrder))

  "addLimitOrder" should "add limit simulator.order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicBuyOrder, 0)

    assert(orderBookSide.getActiveOrders.nonEmpty)
  }

  it should "reject simulator.order of incorrect type simulator.order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicSellOrder, 0)

    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  it should "assign correct priority due to price (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    val higherPricedOrder = Order(OrderType.Buy, bestBuyPrice-1, 10)
    orderBookSide.addLimitOrder(trader, higherPricedOrder, 0)
    orderBookSide.addLimitOrder(trader, basicBuyOrder, 1)

    assert(orderBookSide.getActiveOrders.head.orderId == 1)
  }

  it should "assign correct priority due to arrival time (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicBuyOrder, 0)
    orderBookSide.addLimitOrder(trader, basicBuyOrder, 1)

    assert(orderBookSide.getActiveOrders.head.orderId == 0)
  }

  "addMarketOrder" should "not match in an empty book" in {
    val orderBookSide = emptyOrderBookSide

    assert(orderBookSide.addMarketOrder(trader, basicSellOrder).isDefined)
  }

  it should "match exactly one simulator.order of same size" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize)
    val ret = orderBookSide.addMarketOrder(trader, sellOrder)

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  // TODO: should use 2 different traders here, really
  it should "partially match exactly one active simulator.order of same size" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize-1)
    val ret = orderBookSide.addMarketOrder(trader, sellOrder)

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.head.size == 1)
  }

  it should "partially match exactly one incoming simulator.order" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(trader, basicBuyOrder, 0)
    val sellOrder = Order(OrderType.Sell, bestBuyPrice, standardOrderSize+1)
    val ret = orderBookSide.addMarketOrder(trader, sellOrder)

    assert(ret.get.size == 1)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  "getBestPrice" should "display the best price with one simulator.order" in {
    assert(singularOrderBookSide.getBestPrice.get == bestBuyPrice)
  }

  it should "display the best price with many orders of the same price" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice, 10)
    }).toList
    val orderBookSide = OrderBookSideHelper.getBidSide(orders)

    assert(orderBookSide.getBestPrice.get == bestPrice)
  }

  it should "display the best price with many orders of different prices" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice-x, 10)
    }).toList
    val orderBookSide = OrderBookSideHelper.getBidSide(orders)

    assert(orderBookSide.getBestPrice.get == bestPrice)
  }

  "cancelOrder" should "remove only active simulator.order when given a correct id" in {
    val orderBookSide = singularOrderBookSide
    orderBookSide.cancelOrder(0)

    assert(orderBookSide.getActiveOrders.toList.isEmpty)
  }

  it should "return an simulator.order from active orders when given a correct id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(0).isDefined)
  }

  it should "return a partially filled simulator.order from active orders when given a correct id" in {
    // TODO: this, requires generating orders of the other type
    true
  }

  it should "return None when given an incorrect id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(1).isEmpty)
  }
}
