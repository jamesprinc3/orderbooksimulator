package trader

import java.time.LocalDateTime

import order.OrderType
import orderbook.{OrderBook, OrderBookSide, OrderBookSideType}
import org.scalatest.FlatSpec

class BestPriceRateTraderSpec extends FlatSpec {

  // TODO: remove duplication, perhaps this should be moved out to a test helper class?
  val askSide = new OrderBookSide(OrderBookSideType.Bid)
  val bidSide = new OrderBookSide(OrderBookSideType.Bid)
  val orderBook = new OrderBook(askSide, bidSide)

  // TODO: change this to something more meaningful?
  val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  def getTrader = new BestPriceRateTrader(orderBook, OrderType.Buy, 1, startTime)

  // TODO: the tests in this class feel a bit flimsy, we're assuming the order IDs :/

  "step" should "set the time to one second in the future" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)

    trader.step(newTime)

    assert(trader.getTime == newTime)
  }

  "step" should "submit an order after one second elapsed" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
  }

  "step" should "submit three orders after three seconds have elapsed" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 3)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
    assert(orderBook.getOrder(2).isDefined)
    assert(orderBook.getOrder(3).isDefined)
  }

  "step" should "submit no orders if less than 1 seconds have elapsed" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0, 500)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isEmpty)
  }

  "step" should "submit 2 orders if 2.5 seconds have elapsed" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 2, 500)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
    assert(orderBook.getOrder(2).isDefined)
    assert(orderBook.getOrder(3).isEmpty)
  }
}
