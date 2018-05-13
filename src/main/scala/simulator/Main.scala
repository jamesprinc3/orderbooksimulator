package simulator

import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

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
    val config = getConfig(args.headOption)

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

    println(config)

    val simIndices = if (config.parallel) {
      Range(0, config.numSimulations).par
    } else {
      Range(0, config.numSimulations)
    }

    val simsCompleted = new AtomicInteger(0)

    simIndices.foreach(simulatorNumber => {
      val startTime = LocalDateTime.now()
      val traders = TraderFactory.getRandomTraders(
        1,
        0,
        1,
        10000,
        1,
        config.buyVolumeRatio, // TODO: this should be the wrong metric...
        0.5,
        config.limitOrderRatio,
        config.distributions)
      val orderBook =
        OrderBookFactory.importOrderBook(config.orderBookPath, startTime)
      val simulator =
        new DiscreteEventSimulator(startTime,
                                   startTime.plusNanos((300 * 1e9).toLong),
                                   config.buyCancelRatio,
                                   traders,
                                   List(orderBook))

      val sim_t0 = System.nanoTime()

      try {
        simulator.run()

        logger.debug(orderBook.transactionLog.toString)

        val sim_t1 = System.nanoTime()
        logger.debug(
          s"Simulation $simulatorNumber took: " + ((sim_t1 - sim_t0) / 1e9) + " seconds")

        orderBook.transactionLog.export(simRoot + simulatorNumber + "/")
        traders.foreach(
          t =>
            t.getTransactionLog.export(
              simRoot + simulatorNumber + "/" + t.id.toString))

        simsCompleted.incrementAndGet()
      } catch {
        case e: IllegalStateException =>
          logger.error("Simulation failed: " + e)
      }
    })

    logger.info(simsCompleted.intValue() + "/" + config.numSimulations + " simulations ran")

    val prog_t1 = System.nanoTime()
    logger.info(
      s"Simulations took: " + ((prog_t1 - prog_t0) / 1e9) + " seconds")
  }

  def getConfig(pathOption: Option[String]): Config = {
    println(pathOption)
    val conf = pathOption match {
      case None =>
        ConfigFactory.load()
      case Some(path) =>
        val myConfigFile = new File(path)
        val fileConfig =
          ConfigFactory.parseFile(myConfigFile)
        ConfigFactory.load(fileConfig)
    }

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
