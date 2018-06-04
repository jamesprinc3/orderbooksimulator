package simulator.orderbook

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.Side
import simulator.events.{Cancel, OrderBookEntry}
import simulator.logs.OrderBookLog
import simulator.order.{LimitOrder, MarketOrder, Order}
import simulator.traits.Steppable

import scala.concurrent.duration.Duration

class OrderBook(val askSide: OrderBookSide,
                val bidSide: OrderBookSide,
                orders: List[Order] = List(),
                val transactionLog: OrderBookLog = new OrderBookLog(),
                startTime: LocalDateTime = LocalDateTime.now())
  // TODO: see if we can set this to some kind of default start time
    extends Steppable(startTime) {

  private val _tickLength = Duration.fromNanos(1e6)
  // TODO: add minPrice / maxPrice?
  // Negative prices don't make sense anyway, so should probably put this in

  orders.foreach(order => submitOrder(order, checkTime = false, commitLog = false))

  private val logger = Logger(this.getClass)

  def getBidPrice: Double = {
    bidSide.getBestPrice.getOrElse(0)
  }

  protected[orderbook] def getBidSide: OrderBookSide = {
    bidSide
  }

  def getAskPrice: Double = {
    askSide.getBestPrice.getOrElse(Integer.MAX_VALUE / 2)
  }

  protected[orderbook] def getAskSide: OrderBookSide = {
    askSide
  }

  def getVolume: Double = {
    bidSide.getVolume + askSide.getVolume
  }

  def getMidPrice: Double = {
    (getBidPrice + getAskPrice) / 2
  }

  def submitOrder(order: Order, checkTime: Boolean = true, commitLog: Boolean = true): Unit = {
    if (checkTime && order.time != virtualTime) {
      throw new IllegalStateException("Times do not match")
    }

    if (commitLog) {
      transactionLog.addOrder(order)
    }

    order.trader.updateState(order)
    val trades = (order.side, order) match {
      case (Side.Bid, order: LimitOrder) => bidSide.submitOrder(order)
      case (Side.Bid, order: MarketOrder) => askSide.submitOrder(order)
      case (Side.Ask, order: LimitOrder) => askSide.submitOrder(order)
      case (Side.Ask, order: MarketOrder) => bidSide.submitOrder(order)
    }

    trades match {
      case Some(ts) => ts.foreach(transactionLog.addTrade)
      case None     =>
    }

  }

  def getOrder(orderId: Int): Option[OrderBookEntry] = {
    val allOrders = askSide.getActiveOrders ++ bidSide.getActiveOrders
    allOrders.find(order => order.orderId == orderId)
  }

  def cancelOrder(orderId: Int): Boolean = {
    val cancelledOrder =
      (askSide.cancelOrder(orderId) ++ bidSide.cancelOrder(orderId)).headOption

    if (cancelledOrder.isDefined) {
      val cancel = Cancel(virtualTime, cancelledOrder.get)
      transactionLog.addCancel(cancel)
      true
    } else {
      false
    }
  }

  def getNumberOfOrders: Int = {
    askSide.getActiveOrders.size + bidSide.getActiveOrders.size
  }

  override def step(newTime: LocalDateTime): Unit = {
    super.step(newTime)
    askSide.step(newTime)
    bidSide.step(newTime)
//    logger.debug(
//      virtualTime + " Bid side orders: " + bidSide.getActiveOrders.size)
//    logger.debug(virtualTime + " Bid volume: " + bidSide.getVolume)
//    logger.debug(
//      virtualTime + " Ask side orders: " + askSide.getActiveOrders.size)
//    logger.debug(virtualTime + " Ask volume: " + askSide.getVolume)
  }

}
