package simulator.simulators

import java.time.{LocalDateTime, ZoneOffset}

import com.typesafe.scalalogging.Logger
import simulator.Side
import simulator.order.Order
import simulator.orderbook.OrderBook
import simulator.sampling.TransformedDistr
import simulator.trader.Trader

import scala.collection.mutable
import scala.collection.mutable.PriorityQueue
import scala.util.Random

class DiscreteEventSimulator(startTime: LocalDateTime,
                             endTime: LocalDateTime,
                             buyCancelRatio: Double,
                             inverseCdfs: Map[String, TransformedDistr],
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
    inverseCdfs("buy_cancels_relative")
  private val sellOrderPriceCancellationDistribution =
    inverseCdfs("sell_cancels_relative")

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

  override def updateState(): Boolean = {
    try {
      val event = eventQueue.dequeue()
      val newTime = event._1

      if (newTime.isBefore(virtualTime)) {
        val errString = "Time Machine mode"
        logger.error(errString)
        throw new IllegalStateException(errString)
      }
      numEvents += 1

      val orderbook = orderBooks.head

      val midPrice = orderbook.getMidPrice
      val spread = orderbook.getSpread
      val bidPrice = orderbook.getBidPrice
      val askPrice = orderbook.getAskPrice

      orderbook.orderBookLog.addMidprice(newTime, midPrice)
      orderbook.orderBookLog.addSpread(newTime, spread)
      orderbook.orderBookLog.addBidPrice(newTime, bidPrice)
      orderbook.orderBookLog.addAskPrice(newTime, askPrice)

      if (!orderbook.isValidState) {
        val errString = "Order book state is not valid, terminating simulation early"
        logger.error(errString)
        return false
        //        throw new IllegalStateException(errString)
      }

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
      true
    } catch {
      case e: IllegalStateException => throw e
//      case NonFatal(t) => logger.error("We haz error: " + t)
    }


  }

  // TODO: tidy this up
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
      val sideToCancel = if (isBuySide) {
        Side.Bid
      } else {
        Side.Ask
      }

      orderBook.cancelHead(sideToCancel)

      //      orderBook.cancelOrder(sideToCancel, targetPrice)
    }
  }

  def getQueue(): PriorityQueue[(LocalDateTime, Trader, OrderBook, Order)] = {
    eventQueue
  }

}
