package simulator.simulators

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.orderbook.OrderBook
import simulator.trader.Trader

import scala.concurrent.duration.Duration

// TODO: make this into an interface?
// TODO: better names for input args
class TimeSliceSimulator(startTime: LocalDateTime,
                         private val increment: Duration,
                         private val timeSteps: Int,
                         traders: List[Trader],
                         orderBooks: List[OrderBook])
    extends Simulator(traders, orderBooks) {

  private var elapsedTimeSteps = 0
  private var time = startTime

  private val logger = Logger(this.getClass)

  override def endCondition(): Boolean = {
    elapsedTimeSteps >= timeSteps
  }

  override def updateState(): Unit = {
    time = time.plusNanos(increment.toNanos)
    logger.debug("UpdateState: " + elapsedTimeSteps + " time: " + time.toString)

    // Update the time that each transaction log sees (note, this should have no side effects)
    orderBooks.foreach(_.step(time))
    // Update the time that each trader sees, get the events that each traders wants to do
    val events = traders.flatMap(_.step(time, orderBooks))
    // Submit these orders to the correct OrderBook
    events.foreach(event => event._3.submitOrder(event._4))

    elapsedTimeSteps += 1
  }

  // TODO: maybe implement
  override def initialState(): Unit = {}
}
