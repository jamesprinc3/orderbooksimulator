package simulator.trader

import java.time.LocalDateTime

import breeze.stats.distributions.{ContinuousDistr, Exponential, LogNormal, RandBasis}
import com.typesafe.scalalogging.Logger
import simulator.order.OrderType

import scala.util.Random

object TraderFactory {

  private val logger = Logger(this.getClass)

  def getBasicTraders: List[Trader] = {
    val startTime = LocalDateTime.now()

    val buyParams = TraderParams(17, 100, 1)
    val sellParams = TraderParams(18, 100, 1)

    val buyTrader =
      new BestPriceRateTrader(OrderType.Buy, 100, startTime, buyParams)
    val sellTrader =
      new BestPriceRateTrader(OrderType.Sell, 100, startTime, sellParams)

    List(buyTrader, sellTrader)
  }

  def getHandsOffTrader: Trader = {
    val params = TraderParams(-1, 10, 0)
    new HandsOffTrader(params)
  }

  def getRandomTraders(orderProbability: Double,
                       cancelProbability: Double,
                       n: Int,
                       totalBalance: Double,
                       totalHoldings: Double,
                       distributions: Map[String, ContinuousDistr[Double]]
                      ): List[RandomTrader] = {
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
//          new LogNormal(3.12, 0.8)
        val sellPriceDistribution =
          distributions("sell_price")
//          new Exponential(1.0 / 185.58)

        val buyOrderPriceCancellationDistribution =
          distributions("buy_cancel_price")
        //          new LogNormal(3.11, 0.79)
        val sellOrderPriceCancellationDistribution =
          distributions("sell_cancel_price")
//          new LogNormal(5.26, 0.38)

        val buyRatio = 0.5

        val quantityDistribution =
          distributions("quantity")
          //new Exponential(0.89)

        val intervalDistribution =
          distributions("interval")
          //new Exponential(10)

        new RandomTrader(
          orderProbability,
          cancelProbability,
          buyPriceDistribution,
          sellPriceDistribution,
          buyOrderPriceCancellationDistribution,
          sellOrderPriceCancellationDistribution,
          buyRatio,
          quantityDistribution,
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
