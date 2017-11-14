import java.time.LocalDateTime

import order.OrderType
import orderbook.{OrderBook, OrderBookSide, OrderBookSideType}
import org.scalatest.FlatSpec
import trader.BestPriceRateTrader

class BestPriceRateTraderSpec extends FlatSpec {

  // TODO: remove duplication, perhaps this should be moved out to a test helper class?
  val askSide = new OrderBookSide(OrderBookSideType.Bid)
  val bidSide = new OrderBookSide(OrderBookSideType.Bid)
  val orderBook = new OrderBook(askSide, bidSide)

  // TODO: change this to something more meaningful?
  val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  def getTrader = new BestPriceRateTrader(orderBook, OrderType.Buy, 1, startTime)

  "step" should "set the time to one second in the future" in {
    val trader = getTrader
    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)

    trader.step(newTime)

    assert(trader.getTime == newTime)
  }
}
