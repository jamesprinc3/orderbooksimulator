package simulator.trader

import java.time.LocalDateTime

import mocks.MockPriority
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import simulator.orderbook.priority.Priority
import simulator.orderbook.{OrderBook, TestOrderBook}
import simulator.{Side, TestConstants}

class BestPriceRateTraderSpec extends FlatSpec with BeforeAndAfterEach with MockFactory {

  private val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  private var orderBook: OrderBook = _
  private var traderParams: TraderParams = _
  private var trader: BestPriceRateTrader = _

//  private val mockPriority = new MockPriority()
//
//  override def beforeEach(): Unit = {
//    orderBook = TestOrderBook.getEmptyOrderBook(mockPriority)
//    traderParams = TraderParams(1, 10, 10)
//    trader = new BestPriceRateTrader(Side.Bid, 1, startTime, traderParams)
//  }
//
//  // TODO: the tests in this class feel a bit flimsy, we're assuming the simulator.order IDs :/
//
//  "step" should "set the time to one second in the future" in {
//    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)
//
//    trader.step(newTime)
//
//    assert(trader.getTime == newTime)
//  }
//
//  it should "submit a simulator.order after one second elapsed" in {
//    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 1)
//
//    trader.step(newTime)
//
//    assert(orderBook.getOrder(TestConstants.minOrderIndex).isDefined)
//  }
//
//  it should "submit three orders after three seconds have elapsed" in {
//    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 3)
//
//    trader.step(newTime)
//
//    assert(orderBook.getOrder(TestConstants.minOrderIndex).isDefined)
//    assert(orderBook.getOrder(TestConstants.minOrderIndex + 1).isDefined)
//    assert(orderBook.getOrder(TestConstants.minOrderIndex + 2).isDefined)
//  }
//
//  it should "submit no orders if less than 1 seconds have elapsed" in {
//    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0, 500)
//
//    trader.step(newTime)
//
//    assert(orderBook.getOrder(1).isEmpty)
//  }
//
//  it should "submit 2 orders if 2.5 seconds have elapsed" in {
//
//    val newTime = LocalDateTime.of(2014, 2, 17, 9, 0, 2, 500)
//
//    trader.step(newTime)
//
//    assert(orderBook.getOrder(TestConstants.minOrderIndex).isDefined)
//    assert(orderBook.getOrder(TestConstants.minOrderIndex + 1).isDefined)
//    assert(orderBook.getOrder(TestConstants.minOrderIndex + 2).isEmpty)
//  }
}
