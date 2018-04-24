package simulator

import java.time.LocalDateTime

import simulator.order.{Order, OrderType}
import simulator.orderbook.{OrderBookFactory, OrderBookSide, OrderBookSideType}
import simulator.simulators.TimeSliceSimulator
import simulator.trader.TraderFactory

import scala.concurrent.duration.Duration
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  val filePath = "/Users/jamesprince/project-data/orderbook.csv"

  def main(args: Array[String]): Unit = {
    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(Level.DEBUG)

    val traders = TraderFactory.getRandomTraders(0.2, 0.2, 100, 10000, 1)
    val orderBook = OrderBookFactory.importOrderBook(filePath)
//    val simulator = new DiscreteEventSimulator(
//      LocalDateTime.now(),
//      LocalDateTime.now().plusNanos(10000000),
//      traders,
//      List(orderBook))

    val simulator = new TimeSliceSimulator(LocalDateTime.now(),
                                           Duration.fromNanos(1e6),
      1000,
                                           traders,
                                           List(orderBook))

    simulator.run()

    logger.debug(orderBook.transactionLog.toString)

    orderBook.transactionLog.export("/Users/jamesprince/project-data/sims/")
    traders.foreach(t => t.getTransactionLog.export("/Users/jamesprince/project-data/sims/" + t.id.toString))
  }

  def oldMain(args: Array[String]): Unit = {
    println("Hello world!")

    val startTime = LocalDateTime.now()

    val bestBuyPrice = 99
    val bestSellPrice = 101
    val standardOrderSize = 1
    val basicBuyOrder = Order(OrderType.Buy, bestBuyPrice, standardOrderSize)
    val basicSellOrder = Order(OrderType.Sell, bestSellPrice, standardOrderSize)

    val traders = TraderFactory.getBasicTraders()

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val transactionLog = new TransactionLog()
//    val orderBook = new OrderBook(askSide, bidSide, List(), transactionLog)
//    val orderBook = OrderBookFactory.getPopulatedOrderBook(20)

    val orderBook = OrderBookFactory.importOrderBook(filePath)
//    orderBook.submitOrder(sellTrader, basicSellOrder)
    //    orderBook.submitOrder(buyTrader, basicBuyOrder)

    val simulator = new TimeSliceSimulator(LocalDateTime.now(),
                                           Duration.fromNanos(1e6),
                                           1,
                                           traders,
                                           List(orderBook))

    simulator.run()
    println(orderBook.transactionLog.toString)

    logger.info("Simulation finished")

  }

//  def startSimulation(): Unit = {
//    val trader = BestPriceRateTrader
//  }

}
