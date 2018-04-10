package simulator.simulators

import java.time.LocalDateTime

import org.scalatest.FlatSpec
import simulator.orderbook.OrderBookFactory
import simulator.trader.TraderFactory

import scala.concurrent.duration.Duration

class TimeSliceSimulatorSpec extends FlatSpec {

  val startTime = LocalDateTime.of(2014, 2, 17, 9, 0, 0)
  val increment = Duration.fromNanos(1e6)

  "endCondition" should "be false at the start" in {
    val simulator = new TimeSliceSimulator(startTime, increment, 1, null, null)

    assert(!simulator.endCondition())
  }

  it should "be true after one increment" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getOrderBook())
    val simulator = new TimeSliceSimulator(startTime, increment, 1, traders, orderBooks)

    simulator.updateState()

    assert(simulator.endCondition())
  }

  it should "be true after ten increments" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getOrderBook())
    val simulator = new TimeSliceSimulator(startTime, increment, 10, traders, orderBooks)

    Range(0,10).foreach(_ => simulator.updateState())

    assert(simulator.endCondition())
  }

  "updateState" should "submit one order" in {
    val traders = TraderFactory.getBasicTraders()
    val orderBooks = List(OrderBookFactory.getOrderBook())
    val simulator = new TimeSliceSimulator(startTime, increment, 1, List(traders(1)), orderBooks)

    simulator.updateState()

    assert(simulator.getTransactionLogs.head.orders.length == 1)
  }

}
