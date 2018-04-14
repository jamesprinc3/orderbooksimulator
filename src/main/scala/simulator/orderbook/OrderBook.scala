package simulator.orderbook

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.TransactionLog
import simulator.order.{Order, OrderType, Trade}
import simulator.trader.Trader
import simulator.trader.TraderFactory

import scala.concurrent.duration.Duration

// TODO: poke a hole in this class to allow access to ask/bid sides?
class OrderBook(val askSide: OrderBookSide,
                val bidSide: OrderBookSide,
                orders: List[Order] = List(),
                val transactionLog: TransactionLog = new TransactionLog()) {

  private var _orderId = 0
  private val _tickLength = Duration.fromNanos(1e6)
  private val _lotSize = 1
  // TODO: see if we can set this to some kind of default start time
  private var virtualTime: LocalDateTime = LocalDateTime.now()
  // TODO: add minPrice / maxPrice?
  // Negative prices don't make sense anyway, so should probably put this in

  private val handsOffTrader = TraderFactory.getHandsOffTrader
  orders.foreach(submitOrder(handsOffTrader, _))

  private val logger = Logger(this.getClass)

  def getBidPrice: Double = {
    bidSide.getBestPrice.getOrElse(Integer.MAX_VALUE / 2)
  }

  protected[orderbook] def getBidSide: OrderBookSide = {
    bidSide
  }

  def getAskPrice: Double = {
    askSide.getBestPrice.getOrElse(0)
  }

  protected[orderbook] def getAskSide: OrderBookSide = {
    askSide
  }

  def getPrice: Double = {
    (getBidPrice + getAskPrice) / 2
  }

  private def getOrderID: Int = {
    _orderId += 1
    _orderId
  }

  // TODO: use proper logging instead of println
  def submitOrder(trader: Trader, order: Order): Unit = {
    val orderBookEntry = OrderBookEntry(order.orderType,
                                        trader,
                                        getOrderID,
                                        virtualTime,
                                        order.price,
                                        order.size)
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

    if (askPrice.isEmpty || order.price < askPrice.get) {
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

  def getVolatility(ticks: Int): Double = {
    val windowCutoff = virtualTime.minusNanos(_tickLength.toNanos * ticks)

    val lastTradeBeforeWindow = transactionLog.trades
      .find(trade => trade.time.isBefore(windowCutoff))

    lastTradeBeforeWindow match {
      case None => 0.01
      case Some(_) =>
        val validTrades = transactionLog.trades.filter(trade =>
          trade.time.isAfter(virtualTime.minusNanos(_tickLength.toNanos * ticks)))

        val prices = Range(0, ticks).map(n => {
          val time = windowCutoff.plusNanos(_tickLength.toNanos * n)
          val price = validTrades
            .find(trade => !trade.time.isAfter(time))
            .getOrElse(lastTradeBeforeWindow.get)
            .price

          price
        })

        val mean = prices.sum / ticks
        prices.map(a => math.pow(a - mean, 2)).sum / prices.size
    }
  }

  def step(newTime: LocalDateTime): Unit = {
    logger.debug("Number of orders: " + getNumberOfOrders)
    virtualTime = newTime
  }

}
