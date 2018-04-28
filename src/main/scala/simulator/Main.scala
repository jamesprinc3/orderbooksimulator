package simulator

import java.time.LocalDateTime

import simulator.order.{Order, OrderType}
import simulator.orderbook.{OrderBookFactory, OrderBookSide, OrderBookSideType}
import simulator.simulators.{DiscreteEventSimulator, TimeSliceSimulator}
import simulator.trader.TraderFactory

import scala.concurrent.duration.Duration
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory

import scala.reflect.io.Path

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  val filePath = "/Users/jamesprince/project-data/orderbook.csv"

  def main(args: Array[String]): Unit = {
    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(Level.DEBUG)

    val simsRoot = "/Users/jamesprince/project-data/sims/"
    val simsFile = Path(simsRoot)

    // Flush the directory on each run
    simsFile.deleteRecursively()
    simsFile.createDirectory()
    val orderBook = OrderBookFactory.importOrderBook(filePath)



    // TODO: paralellise this
    Range(0, 1).foreach( simulatorNumber => {
      val startTime = LocalDateTime.now()
      val traders = TraderFactory.getRandomTraders(0.15, 0.15, 10, 10000, 1)
      val orderBook = OrderBookFactory.importOrderBook(filePath)
      val simulator = new DiscreteEventSimulator(
        startTime,
        startTime.plusNanos((7 * 1e9).toLong),
        traders,
        List(orderBook))

//      val simulator = new TimeSliceSimulator(LocalDateTime.now(),
//                                             Duration.fromNanos(1e6),
//        10000,
//                                             traders,
//                                             List(orderBook))

      simulator.run()

      logger.debug(orderBook.transactionLog.toString)

      orderBook.transactionLog.export(simsRoot + simulatorNumber + "/")
      traders.foreach(t => t.getTransactionLog.export(simsRoot + simulatorNumber + "/" + t.id.toString))
    })
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
