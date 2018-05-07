package simulator

import java.time.LocalDateTime

import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import simulator.orderbook.OrderBookFactory
import simulator.simulators.DiscreteEventSimulator
import simulator.trader.TraderFactory
import com.typesafe.config.ConfigFactory

import scala.reflect.io.Path

object Main {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    LoggerFactory
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[Logger]
      .setLevel(Level.INFO)

    val config = getConfig()

//    val parser = new scopt.OptionParser[Config]("scopt") {
//      head("scopt", "3.x")
//
//      opt[String]('s', "sim_root").required() action { (x, c) =>
//        c.copy(simRoot = x) } text "Path to save the simulations to"
//
//      opt[String]('p', "parameters_path").required() action { (x, c) =>
//        val distributions = c.parseDistributions(x)
//        val buyPriceRatio = c.getBuyOrderRatio(x)
//        val buyOrderRatio = c.getBuyVolumeRatio(x)
//        c.copy(distributions = distributions, buyOrderRatio = buyPriceRatio)} text "Path to parameters.json file"
//    }

    // parser.parse returns Option[C]
//    parser.parse(args, Config()) map { config =>
      // arguments are fine
      val simRoot = config.simRoot
      val simPath = Path(simRoot)

      // Flush the directory on each run
      simPath.deleteRecursively()
      simPath.createDirectory()

      val prog_t0 = System.nanoTime()

      println(config.buyOrderRatio)

      Range(0, 100).par.foreach( simulatorNumber => {
        val startTime = LocalDateTime.now()
        val traders = TraderFactory.getRandomTraders(1, 0, 2, 10000, 1, config.buyOrderRatio, config.distributions)
        val orderBook = OrderBookFactory.importOrderBook(config.orderBookPath)
        val simulator = new DiscreteEventSimulator(
          startTime,
          startTime.plusNanos((300 * 1e9).toLong),
          traders,
          List(orderBook))

        val sim_t0 = System.nanoTime()

        simulator.run()

//        logger.debug(orderBook.transactionLog.toString)

        val sim_t1 = System.nanoTime()
        logger.debug(s"Simulation $simulatorNumber took: " + ((sim_t1 - sim_t0) / 1e9) + " seconds")

        orderBook.transactionLog.export(simRoot + simulatorNumber + "/")
        traders.foreach(t => t.getTransactionLog.export(simRoot + simulatorNumber + "/" + t.id.toString))
      })

      val prog_t1 = System.nanoTime()
      logger.info(s"Simulations took: " + ((prog_t1 - prog_t0) / 1e9) + " seconds")
//    } getOrElse {
//      // arguments are bad, usage message will have been displayed
//    }
  }

  def getConfig(): Config = {
    val conf = ConfigFactory.load()
    val simRootPath = conf.getString("paths.sim_root")
    val paramsPath = conf.getString("paths.params")
    val orderBookPath = conf.getString("paths.orderbook")

    Config.init(simRootPath, paramsPath, orderBookPath)
  }
}


