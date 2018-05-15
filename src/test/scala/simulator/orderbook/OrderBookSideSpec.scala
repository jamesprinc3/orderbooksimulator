package simulator.orderbook

import java.time.LocalDateTime

import mocks.{MockPriority, MockTrader}
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import simulator.events.OrderBookEntry
import simulator.order.LimitOrder
import simulator.orderbook.priority.Priority
import simulator.trader.Trader
import simulator.{Side, TestConstants}

class OrderBookSideSpec extends FlatSpec with MockFactory {

  // TODO: sort this dependency mess out
  private def emptyBidSide: OrderBookSide =
    OrderBookSideHelper.getBidSide(List())

  private val mockTrader = new MockTrader()
  private val mockPriority = new MockPriority(Side.Bid, 0)
  private val testTime = LocalDateTime.now()

//  private val orderbook: OrderBook =
//    TestOrderBook.getEmptyOrderBook(mockPriority)

  private val bestBuyPrice = 101
  private val bestSellPrice = 99
  private val standardOrderSize = 10
  val basicBuyOrder = OrderBookEntry(Side.Bid,
                                     mockTrader,
                                     TestConstants.minOrderIndex,
                                     LocalDateTime.now(),
                                     bestBuyPrice,
                                     standardOrderSize)
//  val basicSellOrder = OrderBookEntry(Side.Ask,
//                                      mockTrader,
//                                      TestConstants.minOrderIndex,
//                                      LocalDateTime.now(),
//                                      bestSellPrice,
//                                      standardOrderSize)
  def singularOrderBookSide: OrderBookSide =
    new OrderBookSide(Side.Bid, mockPriority, List(basicBuyOrder))

  //------- addLimitOrder -------

  "addLimitOrder" should "add limit simulator.order to activeOrders" in {
    val orderBookSide = emptyBidSide

    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)

    assert(orderBookSide.getActiveOrders.nonEmpty)
  }

//  it should "call updateState in trader" in {
//    val orderBookSide = emptyBidSide
//
//    (mockTrader.updateState: OrderBookEntry => Unit).expects(basicBuyOrder)
//
//    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
//  }

  it should "assign correct priority due to price (two orders)" in {
    val orderBookSide = emptyBidSide

    val highestPriorityPrice = 10

    orderBookSide.addLimitOrder(mockTrader,
                                highestPriorityPrice - 1,
                                standardOrderSize)
    orderBookSide.addLimitOrder(mockTrader,
                                highestPriorityPrice,
                                standardOrderSize)

    assert(orderBookSide.getActiveOrders.head.price == highestPriorityPrice)
  }

  it should "assign correct priority due to arrival time (two orders)" in {
    val orderBookSide = emptyBidSide

    val price = 10

    orderBookSide.addLimitOrder(mockTrader, price, standardOrderSize)
    orderBookSide.step(LocalDateTime.now())
    orderBookSide.addLimitOrder(mockTrader, price, standardOrderSize + 1)

    assert(orderBookSide.getActiveOrders.last.size == standardOrderSize)
  }

  //------- addMarketOrder -------

  // TODO: figure out whether below tests are needed
  "addMarketOrder" should "throw IllegalStateException in an empty book" in {
    val orderBookSide = emptyBidSide

    assertThrows[IllegalStateException] {
      assert(
        orderBookSide.addMarketOrder(mockTrader, standardOrderSize).isEmpty)
    }
  }

  it should "have empty OrderBook after exactly one order of same size" in {
    val orderBookSide = emptyBidSide

    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
    val ret = orderBookSide.addMarketOrder(mockTrader, standardOrderSize)

    assert(orderBookSide.getActiveOrders.isEmpty)
  }

  it should "have one order in OrderBook after partially matching exactly one active order" in {
    val orderBookSide = emptyBidSide

    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
    orderBookSide.addMarketOrder(mockTrader, standardOrderSize - 1)

    assert(orderBookSide.getActiveOrders.head.size == 1)
  }

  it should "return a trade" in {
    val orderBookSide = emptyBidSide

    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
    val ret = orderBookSide.addMarketOrder(mockTrader, standardOrderSize - 1)

    assert(ret.isDefined)
    assert(ret.get.length == 1)
  }

  it should "return numerous trades when matching multiple orders" in {
    val orderBookSide = emptyBidSide

    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
    orderBookSide.addLimitOrder(mockTrader, bestBuyPrice, standardOrderSize)
    val ret = orderBookSide.addMarketOrder(mockTrader, standardOrderSize * 2)

    assert(ret.isDefined)
    assert(ret.get.length == 2)
  }

  //------- getBestPrice -------

  "getBestPrice" should "display the best price with one simulator.order" in {
    assert(singularOrderBookSide.getBestPrice.get == bestBuyPrice)
  }

  it should "display the best price with many orders of the same price" in {
    val bestPrice = 100
    val orders = Range(0, 3)
      .map(x => {
        LimitOrder(testTime, Side.Bid, mockTrader, bestPrice, 10)
      })
      .toList
    val orderBookSide = OrderBookSideHelper.getBidSide(orders)

    assert(orderBookSide.getBestPrice.get == bestPrice)
  }

  it should "display the best price with many orders of different prices" in {
    val bestPrice = 100
    val orders = Range(0, 3)
      .map(x => {
        LimitOrder(testTime, Side.Bid, mockTrader, bestPrice - x, 10)
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
