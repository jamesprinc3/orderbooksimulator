package simulator.orderbook

import java.time.LocalDateTime

import org.scalatest._
import simulator.TestConstants
import simulator.order.{Order, OrderType}
import simulator.trader.{TestTrader, TraderParams}

class OrderBookSideSpec extends FlatSpec {

  // TODO: sort this dependency mess out
  def emptyOrderBookSide: OrderBookSide = OrderBookSideHelper.getBidSide(List())
  val orderbook: OrderBook = TestOrderBook.getEmptyOrderBook
  val traderParams = TraderParams(0, 10, 10)
  val trader = new TestTrader(traderParams)

  val bestBuyPrice = 101
  val bestSellPrice = 99
  val standardOrderSize = 10
  val basicBuyOrder = OrderBookEntry(OrderType.Buy,
                                     trader,
                                     TestConstants.minOrderIndex,
                                     LocalDateTime.now(),
                                     bestBuyPrice,
                                     standardOrderSize)
  val basicSellOrder = OrderBookEntry(OrderType.Sell,
                                      trader,
                                      TestConstants.minOrderIndex,
                                      LocalDateTime.now(),
                                      bestSellPrice,
                                      standardOrderSize)
  def singularOrderBookSide: OrderBookSide =
    new OrderBookSide(OrderBookSideType.Bid, List(basicBuyOrder))

  //------- addLimitOrder -------

  "addLimitOrder" should "add limit simulator.order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder)

    assert(orderBookSide.getActiveOrders.nonEmpty)
  }

  it should "reject simulator.order of incorrect type simulator.order to activeOrders" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicSellOrder)

    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  it should "assign correct priority due to price (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    val higherPricedOrder = OrderBookEntry(OrderType.Buy,
                                           trader,
                                           TestConstants.minOrderIndex + 1,
                                           LocalDateTime.now(),
                                           bestBuyPrice + 1,
                                           10)
    orderBookSide.addLimitOrder(basicBuyOrder)
    orderBookSide.addLimitOrder(higherPricedOrder)

    assert(
      orderBookSide.getActiveOrders.last.orderId == TestConstants.minOrderIndex)
  }

  it should "assign correct priority due to arrival time (two orders)" in {
    val orderBookSide = emptyOrderBookSide

    val orderWithLaterArrivalTime =
      OrderBookEntry(OrderType.Buy,
                     trader,
                     TestConstants.minOrderIndex + 1,
                     LocalDateTime.now(),
                     bestBuyPrice + 1,
                     10)
    orderBookSide.addLimitOrder(basicBuyOrder)
    orderBookSide.addLimitOrder(orderWithLaterArrivalTime)

    assert(
      orderBookSide.getActiveOrders.last.orderId == TestConstants.minOrderIndex)
  }

  //------- addMarketOrder -------

  // TODO: figure out whether below tests are needed
  "addMarketOrder" should "not match in an empty book" in {
    val orderBookSide = emptyOrderBookSide

    assert(orderBookSide.addMarketOrder(basicBuyOrder)._2.isDefined)
  }

  it should "have empty OrderBook after exactly one order of same size" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder)
    val sellOrder = OrderBookEntry(OrderType.Sell,
                                   trader,
                                   TestConstants.minOrderIndex,
                                   LocalDateTime.now(),
                                   bestBuyPrice,
                                   standardOrderSize)

    val ret = orderBookSide.addMarketOrder(sellOrder)._2

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  // TODO: should use 2 different traders here, really
  it should "have one order in OrderBook after partially matching exactly one active order of same price" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder)
    val sellOrder = OrderBookEntry(OrderType.Sell,
                                   trader,
                                   TestConstants.minOrderIndex,
                                   LocalDateTime.now(),
                                   bestBuyPrice,
                                   standardOrderSize - 1)

    val ret = orderBookSide.addMarketOrder(sellOrder)._2

    assert(ret.isEmpty)
    assert(orderBookSide.getActiveOrders.head.size == 1)
  }

  it should "return partially matched order" in {
    val orderBookSide = emptyOrderBookSide

    orderBookSide.addLimitOrder(basicBuyOrder)
    val sellOrder = OrderBookEntry(OrderType.Sell,
                                   trader,
                                   TestConstants.minOrderIndex,
                                   LocalDateTime.now(),
                                   bestBuyPrice,
                                   standardOrderSize + 1)
    val ret = orderBookSide.addMarketOrder(sellOrder)

    assert(ret._2.get.size == 1)
    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  //------- getBestPrice -------

  "getBestPrice" should "display the best price with one simulator.order" in {
    assert(singularOrderBookSide.getBestPrice.get == bestBuyPrice)
  }

  it should "display the best price with many orders of the same price" in {
    val bestPrice = 100
    val orders = Range(0, 3)
      .map(x => {
        Order(OrderType.Buy, bestPrice, 10)
      })
      .toList
    val orderBookSide = OrderBookSideHelper.getBidSide(orders)

    assert(orderBookSide.getBestPrice.get == bestPrice)
  }

  it should "display the best price with many orders of different prices" in {
    val bestPrice = 100
    val orders = Range(0, 3)
      .map(x => {
        Order(OrderType.Buy, bestPrice + x, 10)
      })
      .toList
    val orderBookSide = OrderBookSideHelper.getBidSide(orders)

    assert(orderBookSide.getBestPrice.get == bestPrice)
  }

  //------- cancelOrder -------

  "cancelOrder" should "remove only active simulator.order when given a correct id" in {
    val orderBookSide = singularOrderBookSide
    orderBookSide.cancelOrder(TestConstants.minOrderIndex)

    assert(orderBookSide.getActiveOrders.toList.isEmpty)
  }

  it should "return an simulator.order from active orders when given a correct id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(TestConstants.minOrderIndex).isDefined)
  }

  it should "return a partially filled simulator.order from active orders when given a correct id" in {
    // TODO: this, requires generating orders of the other type
    true
  }

  it should "return None when given an incorrect id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(TestConstants.minOrderIndex + 1).isEmpty)
  }
}
