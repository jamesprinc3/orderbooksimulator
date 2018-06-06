package simulator.trader

import java.time.LocalDateTime

import breeze.stats.distributions.RandBasis
import com.typesafe.scalalogging.Logger
import simulator.Side
import simulator.sampling.TransformedDistr

import scala.util.Random

object TraderFactory {

  private val logger = Logger(this.getClass)

  def getBasicTraders: List[Trader] = {
    val startTime = LocalDateTime.now()

    val buyParams = TraderParams(17, 100, 1)
    val sellParams = TraderParams(18, 100, 1)

    val buyTrader =
      new BestPriceRateTrader(Side.Bid, 100, startTime, buyParams)
    val sellTrader =
      new BestPriceRateTrader(Side.Ask, 100, startTime, sellParams)

    List(buyTrader, sellTrader)
  }

  def getHandsOffTrader: Trader = {
    val params = TraderParams(-1, 10, 0)
    new HandsOffTrader(params)
  }

  def getRandomTraders(
                        number: Int,
                        ratios: Map[String, Double],
                        correlations: Map[String, Double],
                        distributions: Map[String, TransformedDistr]): List[RandomTrader] = {

    val totalBalance = 0
    val totalHoldings = 0

    Range(0, number)
      .map(x => {
        val traderParams = TraderParams(x,
                                        getWealth(totalBalance, number, x),
                                        getWealth(totalHoldings, number, x))
        val seed = Random.nextInt()
        logger.info("Random seed: " + seed)
        implicit val basis: RandBasis = RandBasis.withSeed(seed)

        logger.info("Ratios: " + distributions)
        logger.info("Correlations: " + distributions)
        logger.info("Distributions: " + distributions)

        new RandomTrader(ratios, correlations, distributions, traderParams)
      })
      .toList
  }

  private def getWealth(totalBalance: Double, numTraders: Int, index: Int) = {
    val eulerMascheroni = 0.577215664901532
    val richest = totalBalance / (Math.log(numTraders) + eulerMascheroni)
    richest / index
  }

}
