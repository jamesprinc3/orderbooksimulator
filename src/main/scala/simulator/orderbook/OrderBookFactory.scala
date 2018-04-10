package simulator.orderbook

import simulator.order.{Order, OrderType}
import breeze.stats.distributions._

object OrderBookFactory {

  def getOrderBook(orders: List[Order] = List()): OrderBook = {

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val orderBook = new OrderBook(askSide, bidSide, orders)

    orderBook
  }

  // TODO: write this method
  /**
    * Returns an order book which has been populated with orders picked from a distribution
    */
  def getPopulatedOrderBook(n: Int): OrderBook = {
    val buySidePrice = new Gaussian(10000, 1000)
    val sellSidePrice = new Gaussian(6000, 1000)

    val buyOrders = Range(0, n).map(x => {
      Order(OrderType.Buy, buySidePrice.sample(), 1)
    }).toList

    val sellOrders = Range(0, n).map(x => {
      Order(OrderType.Sell, sellSidePrice.sample(), 1)
    }).toList

    getOrderBook(buyOrders ++ sellOrders)
  }

}
