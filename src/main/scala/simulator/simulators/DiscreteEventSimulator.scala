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

class DiscreteEventSimulator(startTime: LocalDateTime,
                             endTime: LocalDateTime,
                             buyCancelRatio: Double,
                             traders: List[Trader],
                             orderBooks: List[OrderBook])
  extends OrderBookSimulator(startTime, traders, orderBooks) {


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

  private var numEvents = 0

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
        val errString = "Time Machine mode"
        logger.error(errString)
        throw new IllegalStateException(errString)
      }
      numEvents += 1
      //      if (numEvents % 50 == 0) {

      val orderbook = orderBooks.head
      if (!orderbook.isValidState) {
        val errString = "Order book state is not valid"
        logger.error(errString)
        throw new IllegalStateException(errString)
      }

      orderbook.orderBookLog.addMidprice(newTime, orderbook.getMidPrice)
      orderbook.orderBookLog.addSpread(newTime, orderbook.getSpread)
      orderbook.orderBookLog.addBidPrice(newTime, orderbook.getBidPrice)
      orderbook.orderBookLog.addAskPrice(newTime, orderbook.getAskPrice)

      step(newTime)

      // Update the time that each order book sees
      orderBooks.foreach(_.step(virtualTime))

      // Submit the order to the OrderBook that is given
      event._3.submitOrder(event._4)

      // Cancel a random order with some probability
      if (Random.nextDouble() < 0.90) {
        cancelRandomOrder(event._3)
      }

      // Update the time that the current trader sees and receives any orders to be scheduled
      val eventsToSubmit =
        event._2.step(virtualTime, orderBooks)

      eventsToSubmit.foreach(eventQueue.enqueue(_))

    } catch {
      case e: IllegalStateException => throw e
//      case NonFatal(t) => logger.error("We haz error: " + t)
    }


  }

  private def cancelRandomOrder(orderBook: OrderBook) = {
    val isBuySide = Random.nextFloat() < buyCancelRatio
    val validOrders = if (isBuySide) {
      orderBook.bidSide.getActiveOrders
    } else {
      orderBook.askSide.getActiveOrders
    }
    if (validOrders.nonEmpty) {
      val midPrice = orderBook.getMidPrice

      val targetPrice = if (isBuySide) {
        midPrice - buyOrderPriceCancellationDistribution.sample()
      } else {
        midPrice + sellOrderPriceCancellationDistribution.sample()
      }

      orderBook.cancelOrder(validOrders.head.orderId)
    }
  }

  def getQueue(): PriorityQueue[(LocalDateTime, Trader, OrderBook, Order)] = {
    eventQueue
  }

}
