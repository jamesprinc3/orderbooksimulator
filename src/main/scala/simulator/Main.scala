package simulator

import java.time.LocalDateTime

import ch.qos.logback.classic.Logger
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import simulator.orderbook.OrderBookFactory
import simulator.simulators.DiscreteEventSimulator
import simulator.trader.TraderFactory

import scala.reflect.io.Path

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val config = getConfig

    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(config.logLevel)

    logger.debug(config.toString)

    val simRoot = config.simRoot
    val simPath = Path(simRoot)

    // Flush the output directory on each run
    simPath.deleteRecursively()
    simPath.createDirectory()

    val prog_t0 = System.nanoTime()

    println(config.buyOrderRatio)

    val simIndices = if (config.parallel) {
      Range(0, config.numSimulations).par
    } else {
      Range(0, config.numSimulations)
    }

    simIndices.foreach(simulatorNumber => {
      val startTime = LocalDateTime.now()
      val traders = TraderFactory.getRandomTraders(1,
                                                   0,
                                                   2,
                                                   10000,
                                                   1,
                                                   1 - config.buyOrderRatio,
                                                   config.distributions)
      val orderBook = OrderBookFactory.importOrderBook(config.orderBookPath)
      val simulator =
        new DiscreteEventSimulator(startTime,
                                   startTime.plusNanos((300 * 1e9).toLong),
                                   traders,
                                   List(orderBook))

      val sim_t0 = System.nanoTime()

      simulator.run()

//        logger.debug(orderBook.transactionLog.toString)

      val sim_t1 = System.nanoTime()
      logger.debug(
        s"Simulation $simulatorNumber took: " + ((sim_t1 - sim_t0) / 1e9) + " seconds")

      orderBook.transactionLog.export(simRoot + simulatorNumber + "/")
      traders.foreach(
        t =>
          t.getTransactionLog.export(
            simRoot + simulatorNumber + "/" + t.id.toString))
    })

    val prog_t1 = System.nanoTime()
    logger.info(
      s"Simulations took: " + ((prog_t1 - prog_t0) / 1e9) + " seconds")
  }

  def getConfig: Config = {
    val conf = ConfigFactory.load()
    val numSimulations = conf.getInt("execution.numSimulations")
    val parallel = conf.getBoolean("execution.parallel")
    val logLevel = conf.getString("execution.logLevel")
    val simRootPath = conf.getString("paths.simRoot")
    val paramsPath = conf.getString("paths.params")
    val orderBookPath = conf.getString("paths.orderbook")

    Config.init(numSimulations,
                parallel,
                logLevel,
                simRootPath,
                paramsPath,
                orderBookPath)
  }
}
