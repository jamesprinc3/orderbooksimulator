package trader

import java.time.LocalDateTime

import order.OrderType
import orderbook.{OrderBook, OrderBookSide, OrderBookSideType, TestOrderBook}
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class BestPriceRateTraderSpec extends FlatSpec with BeforeAndAfterEach {

  // TODO: change this to something more meaningful?
  val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  // TODO: make the orderbook common?
  var orderBook: OrderBook = _
  var traderParams: TraderParams = _
  var trader: BestPriceRateTrader = _

  override def beforeEach(): Unit = {
    orderBook = TestOrderBook.getEmptyOrderBook
    traderParams = TraderParams(orderBook, 1, 10, 10)
    trader = new BestPriceRateTrader(OrderType.Buy, 1, startTime, traderParams)
  }

  // TODO: the tests in this class feel a bit flimsy, we're assuming the order IDs :/

  it should "set the time to one second in the future" in {
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)

    trader.step(newTime)

    assert(trader.getTime == newTime)
  }

  it should "submit an order after one second elapsed" in {
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
  }

  it should "submit three orders after three seconds have elapsed" in {
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 3)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
    assert(orderBook.getOrder(2).isDefined)
    assert(orderBook.getOrder(3).isDefined)
  }

  it should "submit no orders if less than 1 seconds have elapsed" in {
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0, 500)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isEmpty)
  }

  it should "submit 2 orders if 2.5 seconds have elapsed" in {

    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 2, 500)

    trader.step(newTime)

    assert(orderBook.getOrder(1).isDefined)
    assert(orderBook.getOrder(2).isDefined)
    assert(orderBook.getOrder(3).isEmpty)
  }
}
