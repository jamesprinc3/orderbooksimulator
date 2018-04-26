package simulator.simulators

import java.time.LocalDateTime

import org.scalatest.FlatSpec
import simulator.orderbook.{OrderBook, OrderBookFactory}
import simulator.trader.{Trader, TraderFactory}

class DiscreteEventSimulatorSpec extends FlatSpec {

  private val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  private val endTime = startTime.plusSeconds(5)
//  val traders: List[Trader]
//  val orderBooks: List[OrderBook]

  "endCondition" should "be false at the start" in {
    val simulator = new DiscreteEventSimulator(startTime, endTime, null, null)

    assert(simulator.endCondition())
  }

  it should "be true when queue is empty" in {
    val simulator = new DiscreteEventSimulator(startTime, endTime, null, null)

    assert(simulator.endCondition())
  }

  it should "be true when currentTime is after endTime" in {
    val simulator = new DiscreteEventSimulator(endTime, startTime, null, null)

    assert(simulator.endCondition())
  }

  "updateState" should "add event to queue when trader returns an order" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getOrderBook())
    val simulator = new DiscreteEventSimulator(startTime, endTime, List(traders(1)), orderBooks)

    simulator.updateState()

    assert(simulator.getQueue().length == 1)
  }

  it should "add events to queue when traders return orders" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getOrderBook())
    val simulator = new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)

    simulator.updateState()

    assert(simulator.getQueue().length == 2)
  }

//  it should "add events in the correct order (by earliest time first)" in {
//    val traders = TraderFactory.getBasicTraders()
//    val orderBooks = List(OrderBookFactory.getOrderBook())
//    val simulator = new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)
//
//    simulator.updateState()
//
//    assert(simulator.getQueue().foldLeft(Lo) )   //((e1, e2) => e1._1.isBefore(e2._1)))
//  }

  "run" should "submit orders inside a simulation" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getPopulatedOrderBook(1))
    val simulator = new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)

    simulator.run()

    assert(simulator.getTransactionLogs.head.orders.nonEmpty)
  }

  it should "execute some trades inside a simulation" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getPopulatedOrderBook(1))
    val simulator = new DiscreteEventSimulator(startTime, endTime, traders, orderBooks)

    simulator.run()

    assert(simulator.getTransactionLogs.head.trades.nonEmpty)
  }

}
