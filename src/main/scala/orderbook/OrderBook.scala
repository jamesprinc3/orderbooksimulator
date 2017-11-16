package orderbook

import order.{Order, OrderType}
import trader.Trader

// TODO: poke a hole in this class to allow access to ask/bid sides?
class OrderBook(askSide: OrderBookSide, bidSide: OrderBookSide) {

  private var _orderId = 0
  private val _tickSize = 1
  private val _lotSize = 1
  // TODO: add minPrice / maxPrice?
  // Negative prices dont make sense anyway, so should probably put this in

  //TODO: transaction record
  // It would be nice to see what has gone on.

  def getBidPrice: Int = {
    bidSide.getBestPrice.getOrElse(return 0)
  }

  def getAskPrice: Int = {
    askSide.getBestPrice.getOrElse(return Integer.MAX_VALUE/2)
  }

  // TODO: perhaps this logic should be moved elsewhere?
  private def getOrderID: Int = {
    _orderId += 1
    _orderId
  }

  def submitOrder(order: Order): Int = {
    order.orderType match {
      case OrderType.Buy => {
        submitBuyOrder(order)
      }
      case OrderType.Sell => {
        submitSellOrder(order)
      }
      case _ =>
        -1
    }
  }

  private def submitBuyOrder(order: Order): Int = {
    val askPrice = askSide.getBestPrice
    val orderId = getOrderID

    if (askPrice.isEmpty || order.price < askPrice.get) {
      bidSide.addLimitOrder(order, orderId)
    } else {
      askSide.addMarketOrder(order)
    }
    orderId
  }

  private def submitSellOrder(order: Order): Int = {
    val bidPrice = askSide.getBestPrice
    val orderId = getOrderID

    if (bidPrice.isEmpty || order.price > bidPrice.get) {
      askSide.addLimitOrder(order, orderId)
    } else {
      bidSide.addMarketOrder(order)
    }
    orderId
  }

  def getOrder(orderId: Int): Option[OrderBookEntry] = {
    val allOrders = askSide.getActiveOrders ++ bidSide.getActiveOrders
    allOrders.find(order => order.orderId == orderId)
  }

  def cancelOrder(orderId: Int): Boolean = {
    askSide.cancelOrder(orderId).isDefined ||
      bidSide.cancelOrder(orderId).isDefined
  }

}
