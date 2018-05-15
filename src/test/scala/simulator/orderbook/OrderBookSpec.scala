package simulator.orderbook

import java.time.LocalDateTime

import mocks.{MockPriority, MockTrader}
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import simulator.order.LimitOrder
import simulator.trader.{TestTrader, TraderParams}
import simulator.{Side, TestConstants}

class OrderBookSpec extends FlatSpec with MockFactory {

  private val initialBalance = 10
  private val initialHoldings = 10

  private var tradeId = 1

  private val testPrice = 13
  private val testSize = 17

  private val mockTrader = new MockTrader()
  private val mockPriority = new MockPriority(Side.Bid, 0)

  private val testTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)

  private def testTrader() = {
    val traderParams =
      TraderParams(tradeId, initialBalance, initialHoldings)
    tradeId += 1
    new TestTrader(traderParams)
  }

  // TODO: make this into some kind of system constant
  "initialization" should "populate OrderBookSides with the correct orders" in {}

  "submitOrder" should "submit a limit buy order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook(mockPriority, mockPriority, testTime)

    orderBook.submitOrder(
      LimitOrder(testTime, Side.Bid, mockTrader, testPrice, testSize))

    assert(orderBook.getBidPrice == testPrice)
  }

  it should "submit a sell order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook(mockPriority, mockPriority, testTime)

    orderBook.submitOrder(
      LimitOrder(testTime, Side.Ask, mockTrader, testPrice, testSize))

    assert(orderBook.getAskPrice == testPrice)
  }

  it should "submit a buy order to bid side" in {
    val orderBook = TestOrderBook.getEmptyOrderBook(mockPriority, mockPriority, testTime)

    orderBook.submitOrder(
      LimitOrder(testTime, Side.Bid, mockTrader, testPrice, testSize))

    assert(orderBook.getBidSide.getActiveOrders.nonEmpty)
  }

  it should "submit a sell order to Ask side" in {
    val orderBook = TestOrderBook.getEmptyOrderBook(mockPriority, mockPriority, testTime)

    orderBook.submitOrder(
      LimitOrder(testTime, Side.Ask, mockTrader, testPrice, testSize))

    assert(orderBook.getAskSide.getActiveOrders.nonEmpty)
  }

  it should "can submit buy and sell orders without match" in {
    val orderBook = TestOrderBook.getEmptyOrderBook(mockPriority, mockPriority, testTime)

    orderBook.submitOrder(
      LimitOrder(testTime, Side.Bid, mockTrader, testPrice, testSize))
    orderBook.submitOrder(
      LimitOrder(testTime, Side.Ask, mockTrader, testPrice + 1, testSize))

    assert(orderBook.getBidSide.getActiveOrders.nonEmpty)
    assert(orderBook.getAskSide.getActiveOrders.nonEmpty)
  }

  "cancelOrder" should "cancel an simulator.order" in {
    val order = LimitOrder(testTime, Side.Bid, mockTrader, testPrice, testSize)
    val orderBook = TestOrderBook.getOrderBook(List(order), mockPriority, testTime)

    assert(orderBook.cancelOrder(TestConstants.minOrderIndex))
  }
}
