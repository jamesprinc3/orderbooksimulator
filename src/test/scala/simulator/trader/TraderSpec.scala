package simulator.trader

import org.scalatest.FlatSpec
import simulator.TestConstants
import simulator.order.{Order, OrderType}
import simulator.orderbook.OrderBookEntry

class TraderSpec extends FlatSpec {

  val initialHoldings = 20
  val initialBalance = 10

  def testTrader() = {
    val traderParams = TraderParams(TestConstants.minOrderIndex, initialBalance, initialHoldings)
    new TestTrader(traderParams)
  }

  "updateState" should "update balance correctly on buy order" in {
    val trader = testTrader()

    val order = Order(OrderType.Buy, 1, 0.5)
    trader.updateState(order)

    assert(trader.getBalance == initialBalance - 0.5)
  }

  it should "not update update holdings on buy order" in {
    val trader = testTrader()

    val order = Order(OrderType.Buy, 1, 0.5)
    trader.updateState(order)

    assert(trader.getHoldings == initialHoldings)
  }

  it should "update holdings correctly on sell order" in {
    val trader = testTrader()

    val order = Order(OrderType.Sell, 1, 2)
    trader.updateState(order)

    assert(trader.getHoldings == initialHoldings - 2)
  }

  it should "not update balance on sell order" in {
    val trader = testTrader()

    val order = Order(OrderType.Sell, 1, 2)
    trader.updateState(order)

    assert(trader.getBalance == initialBalance)
  }

  "cancelOrder" should "update balance correctly on buy order" in {
    val trader = testTrader()

    val order = OrderBookEntry(OrderType.Buy, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getBalance == initialBalance + 2)
  }

  it should "not update holdings on buy order" in {
    val trader = testTrader()

    val order = OrderBookEntry(OrderType.Buy, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getHoldings == initialHoldings)
  }

  it should "update holdings correctly on sell order" in {
    val trader = testTrader()

    val order = OrderBookEntry(OrderType.Sell, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getHoldings == initialHoldings + 2)
  }

  it should "not update balance on sell order" in {
    val trader = testTrader()

    val order = OrderBookEntry(OrderType.Sell, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getBalance == initialBalance)
  }
}
