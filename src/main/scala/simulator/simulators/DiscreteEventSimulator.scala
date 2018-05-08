package simulator.simulators

import java.time.{LocalDateTime, ZoneOffset}

import breeze.stats.distributions.LogNormal
import com.typesafe.scalalogging.Logger
import simulator.order.Order
import simulator.orderbook.OrderBook
import simulator.trader.Trader

import scala.collection.mutable
import scala.collection.mutable.PriorityQueue
import scala.util.Random
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

  private val buyOrderPriceCancellationDistribution =
    new LogNormal(3.11, 0.79)
  private val sellOrderPriceCancellationDistribution =
    new LogNormal(5.26, 0.38)

  override def endCondition(): Boolean = {
    val currentTimeExceedsEndTime = virtualTime.isAfter(endTime)
    val queueIsEmpty = eventQueue.isEmpty && virtualTime != startTime

//    logger.debug("eventQueue size: " + eventQueue.length)

    currentTimeExceedsEndTime || queueIsEmpty
  }

  override def initialState(): Unit = {
    val eventsToSubmit =
      traders.head.initialStep(orderBooks)

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
      event._3.submitOrder(event._4)

      // Cancel a random order with some probability
      if (Random.nextDouble() < 0.9) {
        cancelRandomOrder(event._3)
      }

      // Update the time that each order book sees
      orderBooks.foreach(_.step(virtualTime))

      // Update the time that each trader sees and collate any orders sent back
      val eventsToSubmit =
        event._2.step(virtualTime, orderBooks)

      // Queue up the events
//      if (eventQueue.length < 100) {
        eventsToSubmit.foreach(eventQueue.enqueue(_))
//      }
    } catch {
      case e: IllegalStateException => throw e
//      case NonFatal(t) => logger.error("We haz error: " + t)
    }


  }

  private def cancelRandomOrder(orderBook: OrderBook) = {
    val buyRatio = 0.5
    val isBuySide = Random.nextFloat() < buyRatio
    val validOrders = if (isBuySide) {
      orderBook.bidSide.getActiveOrders
    } else {
      orderBook.askSide.getActiveOrders
    }
    if (validOrders.nonEmpty) {
      val midPrice = orderBook.getPrice

      val targetPrice = if (isBuySide) {
        midPrice - buyOrderPriceCancellationDistribution.sample()
      } else {
        midPrice + sellOrderPriceCancellationDistribution.sample()
      }

      orderBook.cancelOrder(
        validOrders.minBy[Double](o => math.abs(o.price - targetPrice)).orderId)
    }
  }

  def getQueue(): PriorityQueue[(LocalDateTime, Trader, OrderBook, Order)] = {
    eventQueue
  }

}
