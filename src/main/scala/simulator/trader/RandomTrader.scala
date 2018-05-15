package simulator.trader
import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.order.{LimitOrder, MarketOrder, Order}
import simulator.orderbook.OrderBook
import simulator.{Side, TransformedDistr}

import scala.util.Random

/**
  * This class submit random buy and sell orders. It is based on the Random Trader in the paper
  * Using an Artificial Financial Market for studying a Cryptocurrency Market by
  * Cocco, Luisanna; Concas, Giulio; Marchesi, Michele
  */
class RandomTrader(ratios: Map[String, Double],
                   distributions: Map[String, TransformedDistr],
                   traderParams: TraderParams)
    extends Trader(traderParams) {

  private val logger = Logger(this.getClass)
  private val buyPriceDistribution = distributions("buy_price")
  private val sellPriceDistribution = distributions("sell_price")
  private val buyPriceRelativeDistribution = distributions("buy_price_relative")
  private val sellPriceRelativeDistribution = distributions("sell_price_relative")
  private val buyOrderPriceCancellationDistribution = distributions("buy_cancel_price")
  private val sellOrderPriceCancellationDistribution = distributions("sell_cancel_price")
  private val limitOrderSizeDistribution = distributions("limit_size")
  private val marketOrderSizeDistribution: TransformedDistr = distributions("market_size")
  private val intervalDistribution: TransformedDistr= distributions("interval")

  private val buyRatio = ratios("buy_order")
  private val buyVolRatio = ratios("buy_order_vol")
  private val limitOrderRatio = ratios("limit_order")

  private val cancelProbability = 1
  private val orderProbability = 1

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

    val interval = generateInterval(intervalDistribution)
    val orderTime = virtualTime.plusNanos((interval * 1e6).toLong)

    val side = if (Random.nextFloat() < buyRatio) {
      Side.Bid
    } else {
      Side.Ask
    }

    val price = generateOrderPrice(side, midPrice)

    val order = if (Random.nextDouble() < limitOrderRatio) {
      val size = generateOrderSize(limitOrderSizeDistribution)
      LimitOrder(orderTime, side, this, price, size)
    } else {
      val size = generateOrderSize(marketOrderSizeDistribution)
      logger.debug("market order size: " + size)
      MarketOrder(orderTime, side, this, size)
    }
    List((orderTime, this, orderBook, order))
  }

  private def generateOrderPrice(side: Side.Value, midPrice: Double): Double = {
    val price = if (side == Side.Bid) {
      // Buy Order
      midPrice - buyPriceRelativeDistribution.sample()
    } else {
      // Sell Order
      midPrice + sellPriceRelativeDistribution.sample()
    }

    if (price <= 0 || price.isNaN || price.isInfinite || price > midPrice * 3) {
      generateOrderPrice(side, midPrice)
    } else {
      price
    }
  }

  private def generateOrderSize(sizeDist: TransformedDistr): Double = {
    val size = sizeDist.sample()
    if (size <= 0 || size.isNaN || size.isInfinite) {
      generateOrderSize(sizeDist)
    } else {
      size
    }
  }

  private def generateInterval(intervalDist: TransformedDistr): Double = {
    var interval = intervalDistribution.sample()
    while (interval < 0) {
      interval = intervalDistribution.sample()
    }

    interval
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
