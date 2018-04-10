package simulator.simulators

import java.time.{LocalDateTime, ZoneOffset}

import simulator.TransactionLog
import simulator.order.Order
import simulator.orderbook.OrderBook
import simulator.trader.Trader

import scala.collection.mutable.PriorityQueue
import scala.util.control.NonFatal

class DiscreteEventSimulator(startTime: LocalDateTime,
                             endTime: LocalDateTime,
                             traders: List[Trader],
                             orderBooks: List[OrderBook])
    extends Simulator {

  private var virtualTime = startTime
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] =
    Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
  private var eventQueue =
    PriorityQueue.empty[(LocalDateTime, Trader, OrderBook, Order)](
      Ordering.by((_: (LocalDateTime, Trader, OrderBook, Order))._1).reverse)

  override def endCondition(): Boolean = {
    virtualTime.isAfter(endTime) || eventQueue.isEmpty
  }

  override def updateState(): Unit = {
    try {
      val event = eventQueue.dequeue()
      virtualTime = event._1

      // Submit the order to the OrderBook that is given
      event._3.submitOrder(event._2, event._4)
    } catch {
      case NonFatal(t) =>
    }

    // Update the time that each order book sees
    orderBooks.foreach(_.step(virtualTime))

    // Update the time that each trader sees and collate any orders sent back
    val eventsToSubmit =
      traders.map(_.step(virtualTime, orderBooks)).reduce(_ ++ _)

    // Queue up the events
    eventsToSubmit.foreach(eventQueue.enqueue(_))

  }

  def getQueue(): PriorityQueue[(LocalDateTime, Trader, OrderBook, Order)] = {
    eventQueue
  }

  def getTransactionLogs(): List[TransactionLog] = {
    orderBooks.map(_.transactionLog)
  }

}
