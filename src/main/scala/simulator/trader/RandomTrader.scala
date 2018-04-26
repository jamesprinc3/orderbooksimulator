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

  // TODO: this value was previously 0.25
  private val quantityMu = 1
  private val quantitySigma = 0.2

  private val priceMu = 1

  private val logger = Logger(this.getClass)

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, RandomTrader, OrderBook, Order)] = {

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

  private def generateOrder(orderBook: OrderBook) = {

    val beta = Math.max(new LogNormal(quantityMu, quantitySigma).sample(), 0)
    val priceSigma = 0.0001 //k * 0.01 //orderBook.getVolatility(volatilityTicks)
    val norm = new Gaussian(priceMu, priceSigma).sample()
    val midPrice = orderBook.getPrice

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
      val quantity = (this.getBalance * beta) / midPrice
      val price = midPrice * norm

      // TODO: remove duplication of this condition
      if (price <= 0 || price.isNaN || price.isInfinite ||  quantity <= 0 || quantity.isNaN || quantity.isInfinite) {
        List()
      } else {
        List(
          (virtualTime, this, orderBook, Order(OrderType.Buy, price, quantity)))
      }
    } else {

      // Sell Order
      val quantity = this.getHoldings * beta
      val price = midPrice / norm

      if (price <= 0 || price.isNaN || price.isInfinite ||  quantity <= 0 || quantity.isNaN || quantity.isInfinite) {
        List()
      } else {
        List(
          (virtualTime,
           this,
           orderBook,
           Order(OrderType.Sell, price, quantity)))
      }
    }
  }

  // TODO: order abstraction isn't quite right here (we should be passing desires up to the simulator...)
  private def cancelRandomOrder(orderBook: OrderBook) = {
    if (openOrders.nonEmpty) {
      orderBook.cancelOrder(openOrders.toVector(Random.nextInt(openOrders.size)).orderId)
    }
  }
}
