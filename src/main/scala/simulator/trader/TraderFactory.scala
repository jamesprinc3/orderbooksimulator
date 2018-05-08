package simulator.trader

import java.time.LocalDateTime

import breeze.stats.distributions.{
  ContinuousDistr,
  Exponential,
  LogNormal,
  RandBasis
}
import com.typesafe.scalalogging.Logger
import simulator.{Side, TransformedDistr}

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
      orderProbability: Double,
      cancelProbability: Double,
      n: Int,
      totalBalance: Double,
      totalHoldings: Double,
      buyRatio: Double,
      limitOrderRatio: Double,
      distributions: Map[String, TransformedDistr]): List[RandomTrader] = {
    Range(0, n)
      .map(x => {
        val traderParams = TraderParams(x,
                                        getWealth(totalBalance, n, x),
                                        getWealth(totalHoldings, n, x))
        val seed = Random.nextInt()
        logger.info("Random seed: " + seed)
        implicit val basis: RandBasis = RandBasis.withSeed(seed)

        logger.info("Distributions: " + distributions)

        val buyPriceDistribution =
          distributions("buy_price")
        val sellPriceDistribution =
          distributions("sell_price")

        val buyOrderPriceCancellationDistribution =
          distributions("buy_cancel_price")
        val sellOrderPriceCancellationDistribution =
          distributions("sell_cancel_price")

        val limitOrderSizeDistribution =
          distributions("limit_size")

        val marketOrderSizeDistribution =
          distributions("market_size")

        val intervalDistribution =
          distributions("interval")

        new RandomTrader(
          orderProbability,
          cancelProbability,
          buyPriceDistribution,
          sellPriceDistribution,
          buyOrderPriceCancellationDistribution,
          sellOrderPriceCancellationDistribution,
          buyRatio,
          limitOrderRatio,
          limitOrderSizeDistribution,
          marketOrderSizeDistribution,
          intervalDistribution,
          traderParams
        )
      })
      .toList
  }

  private def getWealth(totalBalance: Double, numTraders: Int, index: Int) = {
    val eulerMascheroni = 0.577215664901532
    val richest = totalBalance / (Math.log(numTraders) + eulerMascheroni)
    richest / index
  }

}
