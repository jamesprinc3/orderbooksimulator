package simulator.trader
import java.time.LocalDateTime

import simulator.orderbook.OrderBook

import scala.util.Random
import breeze.stats.distributions._
import com.typesafe.scalalogging.Logger
import simulator.order.{Order, OrderType}

/**
  * This class submit random buy and sell orders. It is based on the Random Trader in the paper
  * Using an Artificial Financial Market for studying a Cryptocurrency Market by
  * Cocco, Luisanna; Concas, Giulio; Marchesi, Michele
  */
class RandomTrader(orderProbability: Double,
                   cancelProbability: Double,
                   traderParams: TraderParams)
    extends Trader(traderParams) {

  private val k = 3.5
  private val volatilityTicks = 20

  private val quantityDistribution = Exponential(2.6)

  private val priceMu = 1
  private val buyPriceDistribution = LogNormal(17.08, 29.87)
  private val sellPriceDistribution = LogNormal(162.10, 209.58)

  private val numTraders = 10
  private val intervalDistribution = new Exponential(20.39/10)

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
    val orderTime = virtualTime.plusNanos((interval * 1e9).toLong)

    val quantity = quantityDistribution.sample()

//    logger.debug(
//      List("beta: ",
//           beta,
//           "priceMu:",
//           priceMu,
//           "priceSigma: :",
//           priceSigma,
//           "norm: ",
//           norm,
//           midPrice).map(_.toString).mkString(" "))

    if (Random.nextFloat() < 0.5) {

      // Buy Order
      val price = midPrice - buyPriceDistribution.sample()

      // TODO: remove duplication of this condition
      if (price <= 0 || price.isNaN || price.isInfinite || quantity <= 0 || quantity.isNaN || quantity.isInfinite) {
        generateOrder(orderBook)
      } else {
        List(
          (orderTime, this, orderBook, Order(OrderType.Buy, price, quantity)))
      }
    } else {

      // Sell Order
      val price = midPrice + sellPriceDistribution.sample()

      if (price <= 0 || price.isNaN || price.isInfinite || quantity <= 0 || quantity.isNaN || quantity.isInfinite || price > midPrice * 3) {
        generateOrder(orderBook)
      } else {
        List(
          (orderTime, this, orderBook, Order(OrderType.Sell, price, quantity)))
      }
    }
  }

  // TODO: order abstraction isn't quite right here (we should be passing desires up to the simulator...)
  private def cancelRandomOrder(orderBook: OrderBook) = {
    if (openOrders.nonEmpty) {
      orderBook.cancelOrder(
        openOrders.toVector(Random.nextInt(openOrders.size)).orderId)
    }
  }
}
