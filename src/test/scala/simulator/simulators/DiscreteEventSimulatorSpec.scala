package simulator.simulators

import java.time.LocalDateTime

import mocks.{MockOrderBook, MockPriority, MockTrader}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import simulator.Side
import simulator.order.Order
import simulator.orderbook.priority.Priority
import simulator.orderbook.{OrderBook, OrderBookFactory}
import simulator.trader.{Trader, TraderFactory}

class DiscreteEventSimulatorSpec extends FlatSpec with MockFactory {

  private val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  private val endTime = startTime.plusSeconds(5)
  private val buyCancelRatio = 0.5

  private val mockTrader = new MockTrader
  private val mockPriority = new MockPriority(Side.Bid, 0)
  private val mockOrderbook = new MockOrderBook()
  private val testTime = LocalDateTime.now()

  private val stubTrader = new MockTrader

  "endCondition" should "be false at the start" in {
    val simulator = new DiscreteEventSimulator(startTime,
                                               endTime,
                                               buyCancelRatio,
                                               List(mockTrader),
                                               List(mockOrderbook))

    assert(!simulator.endCondition())
  }

  it should "be true when currentTime is after endTime" in {
    val simulator = new DiscreteEventSimulator(endTime,
                                               startTime,
                                               buyCancelRatio,
                                               List(mockTrader),
                                               List(mockOrderbook))

    assert(simulator.endCondition())
  }
//
//  "updateState" should "add event to queue when trader returns an order" in {
//    class StubTrader extends MockTrader {
//
//      override def initialStep(newTime: LocalDateTime)
//
//      override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
//        : List[(LocalDateTime, Trader, OrderBook, Order)] = {
//        List((testTime, this, mockOrderbook, mock[Order]))
//      }
//    }
//
//    val t = new StubTrader()
//
//    val simulator = new DiscreteEventSimulator(startTime,
//                                               endTime,
//                                               buyCancelRatio,
//                                               List(t),
//                                               List(mockOrderbook))
//    simulator.initialState()
//    simulator.updateState()
//
//    assert(simulator.getQueue().length == 1)
//  }
//
//  it should "add events to queue when traders return orders" in {
//    (stubTrader.step _)
//      .expects(testTime, List(mockOrderbook))
//      .returns(List((testTime, mockTrader, mockOrderbook, mock[Order]),
//                    (testTime, mockTrader, mockOrderbook, mock[Order])))
//
//    val simulator = new DiscreteEventSimulator(startTime,
//                                               endTime,
//                                               buyCancelRatio,
//                                               List(mockTrader),
//                                               List(mockOrderbook))
//
//    simulator.updateState()
//
//    assert(simulator.getQueue().length == 2)
//  }
//
//  it should "add events in the correct order (by earliest time first)" in {
//    val traders = TraderFactory.getBasicTraders
//    val orderBooks = List(OrderBookFactory.getOrderBook())
//    val simulator =
//      new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)
//
//    simulator.updateState()
//
//    assert(
//      simulator
//        .getQueue()
//        .toList
//        .map(_._1)
//        .sliding(2)
//        .map(list => list.head.isBefore(list(1)))
//        .reduce(_ && _)) // .map(case List(a,b) => a))//.fold((e1: OrderBookEntry, e2: OrderBookEntry) => e1.arrivalTime.isBefore(e2.arrivalTime)) )   //())
//  }
//
//  "run" should "submit orders inside a simulation" in {
//    val traders = TraderFactory.getBasicTraders
//    val orderBooks = List(OrderBookFactory.getPopulatedOrderBook(1))
//    val simulator =
//      new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)
//
//    simulator.run()
//
//    assert(simulator.getTransactionLogs.head.orders.nonEmpty)
//  }
//
//  it should "execute some trades inside a simulation" in {
//    val traders = TraderFactory.getBasicTraders
//    val orderBooks = List(OrderBookFactory.getPopulatedOrderBook(1))
//    val simulator =
//      new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)
//
//    simulator.run()
//
//    assert(simulator.getTransactionLogs.head.trades.nonEmpty)
//  }

}
