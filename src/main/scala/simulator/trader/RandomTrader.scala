package simulator.trader
import java.time.LocalDateTime

import simulator.orderbook.OrderBook

import scala.util.Random
import breeze.stats.distributions._
import simulator.order.{Order, OrderType}

/**
  * This class submit random buy and sell orders. It is based on the Random Trader in the paper
  * Using an Artificial Financial Market for studying a Cryptocurrency Market by
  * Cocco, Luisanna; Concas, Giulio; Marchesi, Michele
  */
class RandomTrader(activityProbability: Double, traderParams: TraderParams)
    extends Trader(traderParams) {

  private val k = 3.5
  private val volatilityTicks = 20

  private val quantityMu = 0.25
  private val quantitySigma = 0.2

  private val priceMu = 1.02

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook]) = {

    // Check whether this trader is active this time
    orderBooks.flatMap(orderBook => {
      if (Random.nextFloat() < activityProbability) {

        val beta = new LogNormal(quantityMu, quantitySigma).sample()
        val priceSigma = k * orderBook.getVolatility(volatilityTicks)
        val norm = new Gaussian(priceMu, priceSigma).sample()
        val midPrice = orderBook.getPrice

        if (Random.nextFloat() < 0.5) {

          // Buy Order
          val quantity = this.getBalance * midPrice * Math.min(1, beta)
          val price = midPrice * norm
          List(
            (virtualTime,
              this,
              orderBook,
              Order(OrderType.Buy, price, quantity)))

        } else {

          // Sell Order
          val quantity = this.getHoldings * beta
          val price = midPrice / norm
          List(
            (virtualTime,
              this,
              orderBook,
              Order(OrderType.Sell, price, quantity)))

        }
      } else {
        List()
      }
    })

  }
}
