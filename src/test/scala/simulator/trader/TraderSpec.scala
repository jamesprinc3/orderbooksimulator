package simulator.trader

import java.time.LocalDateTime

import org.scalatest.FlatSpec
import simulator.TestConstants
import simulator.events.{OrderBookEntry, Trade}
import simulator.order.{Order, Side}

class TraderSpec extends FlatSpec {

  val initialHoldings = 20
  val initialBalance = 10
  val standardPrice = 1
  val standardOrderSize = 2

  val basicBuyOrder = OrderBookEntry(Side.Bid,
                                     testTrader(),
                                     TestConstants.minOrderIndex,
                                     LocalDateTime.now(),
                                     standardPrice,
                                     standardOrderSize)

  val basicSellOrder = OrderBookEntry(Side.Ask,
                                      testTrader(),
                                      TestConstants.minOrderIndex,
                                      LocalDateTime.now(),
                                      standardPrice,
                                      standardOrderSize)

  val basicTrade =
    Trade(LocalDateTime.now(), 0, 0, 1, TestConstants.minOrderIndex, standardPrice, standardOrderSize)

  val mismatchedTrade =
    Trade(LocalDateTime.now(), 0, 0, 1, TestConstants.minOrderIndex + 1, standardPrice, standardOrderSize)

  private def testTrader() = {
    val traderParams =
      TraderParams(TestConstants.minOrderIndex, initialBalance, initialHoldings)
    new TestTrader(traderParams)
  }

  "updateState(order)" should "update balance correctly on buy order" in {
    val trader = testTrader()

    trader.updateState(basicBuyOrder)

    assert(trader.getBalance == initialBalance - standardOrderSize)
  }

  it should "not update update holdings on buy order" in {
    val trader = testTrader()

    trader.updateState(basicBuyOrder)

    assert(trader.getHoldings == initialHoldings)
  }

  it should "update holdings correctly on sell order" in {
    val trader = testTrader()

    trader.updateState(basicSellOrder)

    assert(trader.getHoldings == initialHoldings - standardOrderSize)
  }

  it should "not update balance on sell order" in {
    val trader = testTrader()

    trader.updateState(basicSellOrder)

    assert(trader.getBalance == initialBalance)
  }

  it should "update openOrders with new order" in {
    val trader = testTrader()

    trader.updateState(basicBuyOrder)

    assert(trader.getOpenOrders.size == 1)
  }

  "updateState(trade)" should "remove buy order from openOrders when id matches" in {
    val trader = testTrader()

    trader.updateState(basicBuyOrder)
    trader.updateState(basicTrade)

    assert(trader.getOpenOrders.isEmpty)
  }

  it should "remove sell order from openOrders when id matches" in {
    val trader = testTrader()

    trader.updateState(basicSellOrder)
    trader.updateState(basicTrade)

    assert(trader.getOpenOrders.isEmpty)
  }

  it should "throw when trade is presented with no orders on the book" in {
    val trader = testTrader()

    assertThrows[IllegalStateException](trader.updateState(mismatchedTrade))
  }

  it should "throw when trade is presented with no matching order on the book" in {
    val trader = testTrader()

    trader.updateState(basicSellOrder)

    assertThrows[IllegalStateException](trader.updateState(mismatchedTrade))
  }

  "cancelOrder" should "update balance correctly on buy order" in {
    val trader = testTrader()

    val order = OrderBookEntry(Side.Bid, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getBalance == initialBalance + 2)
  }

  it should "not update holdings on buy order" in {
    val trader = testTrader()

    val order = OrderBookEntry(Side.Bid, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getHoldings == initialHoldings)
  }

  it should "update holdings correctly on sell order" in {
    val trader = testTrader()

    val order = OrderBookEntry(Side.Ask, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getHoldings == initialHoldings + 2)
  }

  it should "not update balance on sell order" in {
    val trader = testTrader()

    val order = OrderBookEntry(Side.Ask, null, 0, null, 1, 2)
    trader.cancelOrder(order)

    assert(trader.getBalance == initialBalance)
  }
}
