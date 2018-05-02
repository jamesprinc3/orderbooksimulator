package simulator

import java.time.LocalDateTime

import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import simulator.orderbook.OrderBookFactory
import simulator.simulators.DiscreteEventSimulator
import simulator.trader.TraderFactory

import scala.reflect.io.Path

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)
  // TODO: move this to program argument
  val filePath = "/Users/jamesprince/project-data/orderbook.csv"

  def main(args: Array[String]): Unit = {
    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(Level.INFO)

    // TODO: move this to program argument
    val simsRoot = "/Users/jamesprince/project-data/sims-2/"
    val simsFile = Path(simsRoot)

    // Flush the directory on each run
    simsFile.deleteRecursively()
    simsFile.createDirectory()

    val prog_t0 = System.nanoTime()

    Range(0, 100).par.foreach( simulatorNumber => {
      val startTime = LocalDateTime.now()
      val traders = TraderFactory.getRandomTraders(1, 1, 2, 10000, 1)
      val orderBook = OrderBookFactory.importOrderBook(filePath)
      val simulator = new DiscreteEventSimulator(
        startTime,
        startTime.plusNanos((300 * 1e9).toLong),
        traders,
        List(orderBook))

      val sim_t0 = System.nanoTime()

      simulator.run()

      logger.debug(orderBook.transactionLog.toString)

      val sim_t1 = System.nanoTime()
      logger.debug(s"Simulation $simulatorNumber took: " + ((sim_t1 - sim_t0) / 1e9) + " seconds")

      orderBook.transactionLog.export(simsRoot + simulatorNumber + "/")
      traders.foreach(t => t.getTransactionLog.export(simsRoot + simulatorNumber + "/" + t.id.toString))
    })

    val prog_t1 = System.nanoTime()
    logger.info(s"Simulations took: " + ((prog_t1 - prog_t0) / 1e9) + " seconds")
  }
}
