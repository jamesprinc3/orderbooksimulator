package simulator.simulators

import java.lang.IllegalStateException
import java.time.{LocalDateTime, ZoneOffset}

import com.typesafe.scalalogging.Logger
import simulator.order.Order
import simulator.orderbook.OrderBook
import simulator.trader.Trader

import scala.collection.mutable
import scala.collection.mutable.PriorityQueue
import scala.util.control.NonFatal

class DiscreteEventSimulator(startTime: LocalDateTime,
                             endTime: LocalDateTime,
                             traders: List[Trader],
                             orderBooks: List[OrderBook])
    extends Simulator(traders, orderBooks) {

  private var virtualTime = startTime
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] =
    Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
  // NOTE: when printing this queue, we don't see the elements in the order they will be dequeued
  private val eventQueue =
    mutable.PriorityQueue.empty[(LocalDateTime, Trader, OrderBook, Order)](
      Ordering.by((_: (LocalDateTime, Trader, OrderBook, Order))._1.toString).reverse)

  private val logger = Logger(this.getClass)

  override def endCondition(): Boolean = {
    val currentTimeExceedsEndTime = virtualTime.isAfter(endTime)
    val queueIsEmpty = eventQueue.isEmpty && virtualTime != startTime

//    logger.debug("eventQueue size: " + eventQueue.length)

    currentTimeExceedsEndTime || queueIsEmpty
  }

  override def initialState(): Unit = {
    val eventsToSubmit =
      traders.map(_.initialStep(orderBooks)).reduce(_++_)

    // Queue up the events
    eventsToSubmit.foreach(eventQueue.enqueue(_))
  }

  override def updateState(): Unit = {
    try {
      val event = eventQueue.dequeue()
      val newTime = event._1

      if (newTime.isBefore(virtualTime)) {
        logger.error("Time Machine mode")
        throw new IllegalStateException("Time Machine mode")
      }
      virtualTime = newTime

//      logger.debug("Virtual Time: " + virtualTime)
//      logger.debug(eventQueue.toString())

      // Submit the order to the OrderBook that is given
      event._3.submitOrder(event._2, event._4)

      // Update the time that each order book sees
      orderBooks.foreach(_.step(virtualTime))

      // Update the time that each trader sees and collate any orders sent back
      val eventsToSubmit =
        traders.map(_.step(virtualTime, orderBooks)).reduce(_++_)

      // Queue up the events
//      if (eventQueue.length < 100) {
        eventsToSubmit.foreach(eventQueue.enqueue(_))
//      }
    } catch {
      case e: IllegalStateException => throw e
      case NonFatal(t) =>
    }


  }

  def getQueue(): PriorityQueue[(LocalDateTime, Trader, OrderBook, Order)] = {
    eventQueue
  }

}
