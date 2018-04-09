package simulator

import java.time.LocalDateTime

import simulator.orderbook.OrderBook
import simulator.trader.Trader

import scala.concurrent.duration.Duration

// TODO: make this into an interface?
// TODO: better names for input args
class Simulator(startTime: LocalDateTime,
                private val increment: Duration,
                private val timeSteps: Int,
                traders: List[Trader],
                orderBooks: List[OrderBook]) {

  private var elapsedTimeSteps = 0
  private var time = startTime

  def run(): Unit = {
    while (elapsedTimeSteps < timeSteps) {
      time.plusNanos(increment.toNanos)
      // Update the time that each transaction log sees (note, this should have nop side effects)
      orderBooks.foreach(_.step(time))
      // Update the time that each trader sees
      traders.foreach(_.step(time, orderBooks))
      elapsedTimeSteps += 1
    }
  }

}
