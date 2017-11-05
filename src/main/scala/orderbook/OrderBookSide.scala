package orderbook

import java.util.PriorityQueue

import order.Order

class OrderBookSide {

  private val queue = new PriorityQueue[OrderBookEntry]()

  def addLimitOrder(order: Order): Unit = {

  }

  def addMarketOrder(order: Order): Unit = {

  }

  def getBestPrice(): Int = {
    0
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
