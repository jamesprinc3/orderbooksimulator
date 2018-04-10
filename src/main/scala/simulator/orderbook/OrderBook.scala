package simulator.orderbook

import java.time.LocalDateTime

import simulator.TransactionLog
import simulator.order.{Order, OrderType, Trade}
import simulator.trader.Trader
import simulator.trader.TraderFactory

// TODO: poke a hole in this class to allow access to ask/bid sides?
class OrderBook(askSide: OrderBookSide,
                bidSide: OrderBookSide,
                orders: List[Order] = List(),
                val transactionLog: TransactionLog = new TransactionLog()) {

  private var _orderId = 0
  private val _tickSize = 1
  private val _lotSize = 1
  // TODO: see if we can set this to some kind of default start time
  private var virtualTime: LocalDateTime = LocalDateTime.now()
  // TODO: add minPrice / maxPrice?
  // Negative prices don't make sense anyway, so should probably put this in

  private val handsOffTrader = TraderFactory.getHandsOffTrader
  orders.foreach(submitOrder(handsOffTrader, _))

  def getBidPrice: Double = {
    bidSide.getBestPrice.getOrElse(Integer.MAX_VALUE/2)
  }

  def getAskPrice: Double = {
    askSide.getBestPrice.getOrElse(0)
  }

  private def getOrderID: Int = {
    _orderId += 1
    _orderId
  }

  // TODO: use proper logging instead of println
  def submitOrder(trader: Trader, order: Order): Unit = {
    println("order submitted")
    val orderBookEntry = OrderBookEntry(order.orderType, trader, getOrderID, virtualTime, order.price, order.size)
    transactionLog.addOrder(orderBookEntry)
    order.orderType match {
      case OrderType.Buy =>
        submitBuyOrder(orderBookEntry)
      case OrderType.Sell =>
        submitSellOrder(orderBookEntry)
    }
  }

  private def submitBuyOrder(order: OrderBookEntry): Unit = {
    val askPrice = askSide.getBestPrice

    if (askPrice.isEmpty || order.price > askPrice.get) {
      bidSide.addLimitOrder(order)
    } else {
      val (trades: Option[List[Trade]], _) = askSide.addMarketOrder(order)
      trades.get.foreach(transactionLog.addTrade)
    }
  }

  private def submitSellOrder(order: OrderBookEntry): Unit = {
    val bidPrice = bidSide.getBestPrice

    if (bidPrice.isEmpty || order.price > bidPrice.get) {
      askSide.addLimitOrder(order)
    } else {
      val (trades: Option[List[Trade]], _) = bidSide.addMarketOrder(order)
      trades.get.foreach(transactionLog.addTrade)
    }
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

  def step(newTime: LocalDateTime): Unit = {
    virtualTime = newTime
  }

}
