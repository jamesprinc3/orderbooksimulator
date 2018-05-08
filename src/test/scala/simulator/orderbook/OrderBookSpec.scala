package simulator.orderbook

import simulator.order.{Order, Side}
import org.scalatest._
import simulator.TestConstants
import simulator.trader.{TestTrader, TraderParams}

class OrderBookSpec extends FlatSpec {

  private val initialBalance = 10
  private val initialHoldings = 10

  private var tradeId = 1

  private def testTrader() = {
    val traderParams =
      TraderParams(tradeId, initialBalance, initialHoldings)
    tradeId += 1
    new TestTrader(traderParams)
  }

  // TODO: make this into some kind of system constant
  "initialization" should "populate OrderBookSides with the correct orders" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val order = Order(Side.Bid, 10, 10)
    orderBook.submitOrder(testTrader(), order)

    assert(orderBook.getBidPrice == 10)
  }

  "submitOrder" should "submit a buy simulator.order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val order = Order(Side.Bid, 10, 10)
    orderBook.submitOrder(testTrader(), order)

    assert(orderBook.getBidPrice == 10)
  }

  it should "submit a sell order" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val order = Order(Side.Ask,10, 10)
    orderBook.submitOrder(testTrader(), order)

    assert(orderBook.getAskPrice == 10)
  }

  it should "submit a buy order to bid side" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val order = Order(Side.Bid, 10, 10)
    orderBook.submitOrder(testTrader(), order)

    val enterredOrder = orderBook.getBidSide.getActiveOrders.head

    assert(enterredOrder.side == order.orderType)
    assert(enterredOrder.price == order.price)
    assert(enterredOrder.size == order.size)
  }

  it should "submit a sell order to Ask side" in {
    val orderBook = TestOrderBook.getEmptyOrderBook

    val order = Order(Side.Ask, 10, 10)
    orderBook.submitOrder(testTrader(), order)

    val enterredOrder = orderBook.getAskSide.getActiveOrders.head

    assert(enterredOrder.side == order.orderType)
    assert(enterredOrder.price == order.price)
    assert(enterredOrder.size == order.size)
  }

  it should "submit buy and sell and not match when buyPrice < sellPrice" in {
    val orderBook = TestOrderBook.getEmptyOrderBook
    val buyPrice = 10
    val sellPrice = 20

    val buyOrder = Order(Side.Bid, buyPrice, 10)
    val sellOrder = Order(Side.Ask, sellPrice, 10)

    orderBook.submitOrder(testTrader(), buyOrder)
    orderBook.submitOrder(testTrader(), sellOrder)

    assert(orderBook.getBidSide.getActiveOrders.size == 1)
    assert(orderBook.getAskSide.getActiveOrders.size == 1)
  }

  it should "submit buy and sell match when buyPrice >= sellPrice" in {
    val orderBook = TestOrderBook.getEmptyOrderBook
    val buyPrice = 20
    val sellPrice = 10

    val buyOrder = Order(Side.Bid, buyPrice, 10)
    val sellOrder = Order(Side.Ask, sellPrice, 10)

    val buyTrader = testTrader()
    val sellTrader = testTrader()

    orderBook.submitOrder(buyTrader, buyOrder)
    orderBook.submitOrder(sellTrader, sellOrder)

    assert(orderBook.getBidSide.getActiveOrders.isEmpty)
    assert(orderBook.getAskSide.getActiveOrders.isEmpty)
  }

  "cancelOrder" should "cancel an simulator.order" in {
    val order = Order(Side.Ask, 10, 10)
    val orderBook = TestOrderBook.getOrderBook(List(order))

    assert(orderBook.cancelOrder(TestConstants.minOrderIndex))
  }
}
