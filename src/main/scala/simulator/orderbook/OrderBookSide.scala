package simulator.orderbook

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.Side
import simulator.events.{OrderBookEntry, Trade}
import simulator.order.{LimitOrder, MarketOrder, Order}
import simulator.orderbook.priority.Priority
import simulator.trader.Trader

import scala.collection.mutable
import util.control.Breaks._

class OrderBookSide(side: Side.Value,
                    priority: Priority,
                    orders: List[OrderBookEntry] = List()) {

  private val logger = Logger(this.getClass)
  private var virtualTime = LocalDateTime.now()
  private implicit val ordering = priority.ordering
  private var _orderId = side match {
    case Side.Bid => 0
    case Side.Ask => 0
  }

  private def getOrderID: Int = {
    _orderId += 1
    _orderId
  }

  def step(newTime: LocalDateTime): Unit = {
    virtualTime = newTime
  }

  def submitOrder(order: Order): Option[List[Trade]] = {
    if (order.side != side) {
      throw new IllegalAccessError(
        "Order side is " + order.side + " should be " + side)
    }

    order match {
      case LimitOrder(_, _, trader, price, size) =>
        addLimitOrder(trader, price, size)
        None
      case MarketOrder(_, _, trader, size) =>
        addMarketOrder(trader, size)
    }
  }

  private var activeOrders = mutable.TreeSet[OrderBookEntry]().++(orders)

  def getActiveOrders: mutable.TreeSet[OrderBookEntry] = {
    activeOrders
  }

  // TODO: Error handling
  // TODO: is adding an order with the same ID as an existing order a fail?
  // TODO: split the order handling off into a different class?
  def addLimitOrder(trader: Trader, price: Double, size: Double): Unit = {
    val entry =
      OrderBookEntry(side, trader, getOrderID, virtualTime, price, size)

    activeOrders.+=(entry)
    trader.updateState(entry)
  }

  // TODO: Error handling
  // TODO: Do market orders just have a size, rather than a price?
  // TODO: Add an ID in here too? I think it makes sense to have that in the transaction log!
  /**
    * @return None if the order was matched in its entirety.
    *         Some(order) if we were unable to match the simulator.order fully, in this situation the simulator.order book can
    *         choose whether to re-enter this as a limit simulator.order.
    */
  def addMarketOrder(
      trader: Trader,
      size: Double): Option[List[Trade]] = {
    val iter = activeOrders.iterator
    var openOrder: OrderBookEntry = null
    var remainingSize = size
    var tradesThatHappened: List[Trade] = List[Trade]()
    while (iter.hasNext && remainingSize > 0) {
      openOrder = iter.next()

//      breakable {
//        if (openOrder.trader.id == trader.id) {
//          break
//        } else {

      val trade = reconcile(openOrder, trader, remainingSize)
      tradesThatHappened = tradesThatHappened ++ List(trade)
      activeOrders.remove(openOrder)
      remainingSize -= openOrder.size
//        }
//      }
    }

    if (remainingSize > 0) {
      // We have run out of active orders on this side of the book, which is pretty bad news
      logger.error("0 orders remain on the " + side + " side")
    }

    if (remainingSize < 0) {
      // Re-add the partially matched open order to this OrderBookSide
      val newPartialOrder = openOrder.copy(size = -1 * remainingSize)
      addLimitOrder(openOrder.trader, openOrder.price, openOrder.size)
    }
    Some(tradesThatHappened)
  }

  protected[orderbook] def reconcile(openOrder: OrderBookEntry,
                                     trader: Trader,
                                     remainingSize: Double): Trade = {
    val taker = openOrder.trader
    val maker = trader
    val orderId = getOrderID
    val trade = side match {
      case Side.Bid =>
        Trade(virtualTime,
              taker.id,
              openOrder.orderId,
              maker.id,
              orderId,
              openOrder.price,
              Math.min(remainingSize, openOrder.size))
      case Side.Ask =>
        Trade(virtualTime,
              maker.id,
              orderId,
              taker.id,
              openOrder.orderId,
              openOrder.price,
              Math.min(remainingSize, openOrder.size))
    }

    //    try {
    maker.updateState(trade)
    taker.updateState(trade)
    //    } catch {
    //      case e: IllegalStateException =>
    //      case e                        => logger.error(e.toString)
    //    }

    trade
  }

//  protected[orderbook] def reconcile(openOrder: OrderBookEntry,
//                                     incomingOrder: OrderBookEntry): Trade = {
//    val taker = openOrder.trader
//    val maker = incomingOrder.trader
//    val trade = side match {
//      case Side.Bid =>
//        // TODO: LocalDateTime needs to change to something more meaningful
//        Trade(incomingOrder.time,
//              taker.id,
//              openOrder.orderId,
//              maker.id,
//              incomingOrder.orderId,
//              openOrder.price,
//              Math.min(incomingOrder.size, openOrder.size))
//      case Side.Ask =>
//        Trade(incomingOrder.time,
//              maker.id,
//              incomingOrder.orderId,
//              taker.id,
//              openOrder.orderId,
//              openOrder.price,
//              Math.min(incomingOrder.size, openOrder.size))
//    }
//
////    try {
//      maker.updateState(trade)
//      taker.updateState(trade)
////    } catch {
////      case e: IllegalStateException =>
////      case e                        => logger.error(e.toString)
////    }
//
//    trade
//  }

  private def getOrdersAtPrice(price: Double): Iterator[OrderBookEntry] = {
    activeOrders.filter(order => order.price == price).iterator
  }

  // TODO: consider accuracy of doubles...
  private def getDepth(price: Double): Double = {
    getOrdersAtPrice(price).map(_.size).sum
  }

  def cancelOrder(orderId: Int): Option[OrderBookEntry] = {
    activeOrders.find(order => order.orderId == orderId) match {
      case None => None
      case Some(orderToCancel) =>
        orderToCancel.trader.cancelOrder(orderToCancel)
        activeOrders.remove(orderToCancel)
        Some(orderToCancel)
    }

  }

  def getBestPrice: Option[Double] = {
    val bestOrder = activeOrders.headOption

    if (bestOrder.isEmpty) {
      None
    } else {
      Some(bestOrder.get.price)
    }
  }

  def getVolume: Double = {
    activeOrders.map(_.size).sum
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the simulator.order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
