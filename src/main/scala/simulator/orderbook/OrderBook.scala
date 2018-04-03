package simulator.orderbook

import simulator.TradeLog
import simulator.order.{Order, OrderType, Trade}
import simulator.trader.Trader
import simulator.trader.TraderFactory

// TODO: poke a hole in this class to allow access to ask/bid sides?
class OrderBook(askSide: OrderBookSide, bidSide: OrderBookSide, orders: List[Order] = List()) {

  private val handsOffTrader = TraderFactory.getHandsOffTrader
  orders.foreach(submitOrder(handsOffTrader, _))

  private var _orderId = 0
  private val _tickSize = 1
  private val _lotSize = 1
  val tradeLog = new TradeLog()
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

  def submitOrder(trader: Trader, order: Order): Int = {
    println("order submitted")
    order.orderType match {
      case OrderType.Buy =>
        submitBuyOrder(trader, order)
      case OrderType.Sell =>
        submitSellOrder(trader, order)
      case _ =>
        -1
    }
  }

  private def submitBuyOrder(trader: Trader, order: Order): Int = {
    val askPrice = askSide.getBestPrice
    val orderId = getOrderID

    if (askPrice.isEmpty || order.price > askPrice.get) {
      bidSide.addLimitOrder(trader, order, orderId)
    } else {
      val (trades: Option[List[Trade]], _) = askSide.addMarketOrder(trader, order, orderId)
      trades.get.foreach(tradeLog.addTrade)
    }
    orderId
  }

  private def submitSellOrder(trader: Trader, order: Order): Int = {
    val bidPrice = bidSide.getBestPrice
    val orderId = getOrderID

    if (bidPrice.isEmpty || order.price < bidPrice.get) {
      askSide.addLimitOrder(trader, order, orderId)
    } else {
      val (trades: Option[List[Trade]], _) = bidSide.addMarketOrder(trader, order, orderId)
      trades.get.foreach(tradeLog.addTrade)
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

  def getNumberOfOrders: Int = {
    askSide.getActiveOrders.size + bidSide.getActiveOrders.size
  }


}
