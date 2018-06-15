package simulator

import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

import ch.qos.logback.classic.Logger
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import simulator.logs.LogWriter
import simulator.orderbook.OrderBookFactory
import simulator.orderbook.OrderBookFactory.getOrderBook
import simulator.simulators.{DiscreteEventSimulator, RandomWalkSimulator}
import simulator.trader.TraderFactory

import scala.reflect.io.Path

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val conf = loadConfig(args.headOption)
    val globalConfig = parseConfig(conf)

    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(globalConfig.logLevel)

    logger.debug(globalConfig.toString)

    simulateOrderBooks(conf: config.Config, globalConfig: Config)

  }

  private def randomWalk(globalConfig: Config) = {
    val replicationIndices = getReplicationIndices(globalConfig)

    replicationIndices.foreach(x => {

      val randomWalkSim = new RandomWalkSimulator(100, 100)
      randomWalkSim.run()

      //      print(randomWalkSim.log.toCsvString)
      LogWriter.write(randomWalkSim.log,
        s"/Users/jamesprince/project-data/random-walk/$x.csv")
    })
  }

  private def getReplicationIndices(globalConfig: Config) = {
    if (globalConfig.parallel) {
      Range(0, globalConfig.numReplications).par
    } else {
      Range(0, globalConfig.numReplications)
    }
  }

  private def simulateOrderBooks(conf: config.Config,
                                 globalConfig: Config): Unit = {
    val simRoot = globalConfig.simRoot
    val simPath = Path(simRoot)

    // Flush the output directory on each run
    simPath.deleteRecursively()
    simPath.createDirectory()

    val prog_t0 = System.nanoTime()

    println(globalConfig)

    val replicationIndices = getReplicationIndices(globalConfig)

    val successfulReplications = new AtomicInteger(0)

    val startTime = LocalDateTime.now()

    val orders =
      OrderBookFactory.importOrders(globalConfig.orderBookPath, startTime)

    val conf_parse_t0 = System.nanoTime()
    val config = parseConfig(conf)
    val conf_parse_t1 = System.nanoTime()
    logger.info("Config parsing took: " + ((conf_parse_t1 - conf_parse_t0) / 1e9) + " seconds")

    replicationIndices.foreach(replicationNumber => {


      val numTraders = config.numTraders

      val traders =
        TraderFactory.getRandomTraders(numTraders,
          config.ratios,
          config.correlations,
          config.inverseCdfDistributions,
          config.distributions)
      val orderBook = getOrderBook(orders)
      val simulator =
        new DiscreteEventSimulator(
          startTime,
          startTime.plusNanos((config.simulationSeconds * 1e9).toLong),
          config.ratios("buy_sell_cancel_ratio"),
          config.inverseCdfDistributions,
          traders,
          List(orderBook))

      val sim_t0 = System.nanoTime()

      try {
        simulator.run()

        logger.debug(orderBook.orderBookLog.toString)

        val sim_t1 = System.nanoTime()
        logger.debug(
          s"Simulation $replicationNumber took: " + ((sim_t1 - sim_t0) / 1e9) + " seconds")

        // Add final order book state to log
        orderBook.getOrders.foreach(orderBook.orderBookLog.addOrderBookEntry)

        // Write out logs to CSV files
        orderBook.orderBookLog.export(simRoot + replicationNumber + "/")

        // Only write out per-trader data if multiple traders
        if (numTraders > 1) {
          traders.foreach(
            t =>
              t.getTransactionLog.export(
                simRoot + replicationNumber + "/" + t.id.toString))
        }

        if (simulator.success) {
          successfulReplications.incrementAndGet()
        }
      } catch {
        case e: IllegalStateException =>
          logger.error("Simulation failed: " + e)
      }
    })

    logger.info(
      successfulReplications
        .intValue() + "/" + globalConfig.numReplications + " replications were successful")

    val prog_t1 = System.nanoTime()
    logger.info(
      s"Simulations took: " + ((prog_t1 - prog_t0) / 1e9) + " seconds")
  }

  def loadConfig(pathOption: Option[String]): com.typesafe.config.Config = {
    println(pathOption)
    pathOption match {
      case None =>
        ConfigFactory.load()
      case Some(path) =>
        val myConfigFile = new File(path)
        val fileConfig =
          ConfigFactory.parseFile(myConfigFile)
        ConfigFactory.load(fileConfig)
    }
  }

  def parseConfig(conf: com.typesafe.config.Config): Config = {
    val numSimulations = conf.getInt("execution.numSimulations")
    val numTraders = conf.getInt("execution.numTraders")
    val simulationSeconds = conf.getInt("execution.simulationSeconds")
    val parallel = conf.getBoolean("execution.parallel")
    val decimalPlaces = conf.getInt("precision.decimalPlaces")
    val logLevel = conf.getString("execution.logLevel")
    val simRootPath = conf.getString("paths.simRoot")
    val paramsPath = conf.getString("paths.params")
    val orderBookPath = conf.getString("paths.orderbook")

    Config.init(numSimulations,
                simulationSeconds,
                numTraders,
                parallel,
      decimalPlaces,
                logLevel,
                simRootPath,
                paramsPath,
                orderBookPath)
  }
}
