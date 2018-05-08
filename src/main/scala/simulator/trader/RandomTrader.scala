package simulator.trader
import java.time.LocalDateTime

import breeze.stats.distributions._
import com.typesafe.scalalogging.Logger
import simulator.{Side, TransformedDistr}
import simulator.order.{LimitOrder, MarketOrder, Order}
import simulator.orderbook.OrderBook

import scala.util.Random

/**
  * This class submit random buy and sell orders. It is based on the Random Trader in the paper
  * Using an Artificial Financial Market for studying a Cryptocurrency Market by
  * Cocco, Luisanna; Concas, Giulio; Marchesi, Michele
  */
class RandomTrader(orderProbability: Double,
                   cancelProbability: Double,
                   buyPriceDistribution: TransformedDistr,
                   sellPriceDistribution: TransformedDistr,
                   buyOrderPriceCancellationDistribution: TransformedDistr,
                   sellOrderPriceCancellationDistribution: TransformedDistr,
                   buyRatio: Double,
                   limitOrderRatio: Double,
                   sizeDistribution: TransformedDistr,
                   intervalDistribution: TransformedDistr,
                   traderParams: TraderParams)
    extends Trader(traderParams) {

  private val logger = Logger(this.getClass)

  override def initialStep(orderBooks: List[OrderBook])
    : List[(LocalDateTime, RandomTrader, OrderBook, Order)] = {
    orderBooks.map(generateOrder).reduce(_ ++ _)
  }

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, RandomTrader, OrderBook, Order)] = {

    virtualTime = newTime

    orderBooks.flatMap(orderBook => {
      if (Random.nextFloat() < cancelProbability) {
        cancelRandomOrder(orderBook)
      }

      if (Random.nextFloat() < orderProbability) {
        generateOrder(orderBook)
      } else {
        List()
      }
    })

  }

  private def generateOrder(orderBook: OrderBook)
    : List[(LocalDateTime, RandomTrader, OrderBook, Order)] = {

//    val priceSigma = 0.001 //k * 0.01 //orderBook.getVolatility(volatilityTicks)
    val midPrice = orderBook.getPrice

    val interval = intervalDistribution.sample()
    if (interval < 0) {
      logger.error("interval is negative")
    }
    val orderTime = virtualTime.plusNanos((interval * 1e6).toLong)

    val size = sizeDistribution.sample()

    val price = if (Random.nextFloat() < buyRatio) {
      // Buy Order
      midPrice - buyPriceDistribution
        .sample() //  - ((10.24 / 22.75) * 10))
    } else {
      // Sell Order
      midPrice + sellPriceDistribution
        .sample() //- ((125.01 / 185.58) * 10))
    }

    if (price <= 0 || price.isNaN || price.isInfinite || size <= 0 || size.isNaN || size.isInfinite || price > midPrice * 3) {
      generateOrder(orderBook)
    } else {
      val order = if (Random.nextDouble() < limitOrderRatio) {
        LimitOrder(Side.Bid, this, price, size)
      } else {
        MarketOrder(Side.Bid, this, size)
      }
      List((orderTime, this, orderBook, order))
    }

  }

  // TODO: order abstraction isn't quite right here (we should be passing desires up to the simulator...)
  // TODO: maybe remove duplication
  private def cancelRandomOrder(orderBook: OrderBook) = {
    val isBuySide = Random.nextFloat() < buyRatio
    val validOrders = if (isBuySide) {
      openOrders.filter(_.side == Side.Bid)
    } else {
      openOrders.filter(_.side == Side.Ask)
    }
    if (validOrders.nonEmpty) {
      val midPrice = orderBook.getPrice

      val targetPrice = if (isBuySide) {
        midPrice - buyOrderPriceCancellationDistribution.sample()
      } else {
        midPrice + sellOrderPriceCancellationDistribution.sample()
      }

      orderBook.cancelOrder(
        validOrders.minBy[Double](o => math.abs(o.price - targetPrice)).orderId)

    }
  }
}
