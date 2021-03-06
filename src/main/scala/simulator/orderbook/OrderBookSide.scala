package simulator.orderbook

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import simulator.Side
import simulator.events.{OrderBookEntry, Trade}
import simulator.order.{LimitOrder, MarketOrder, Order}
import simulator.orderbook.priority.Priority
import simulator.trader.Trader
import simulator.traits.Steppable

import scala.collection.mutable

class OrderBookSide(side: Side.Value,
                    priority: Priority,
                    orders: List[OrderBookEntry] = List())
    extends Steppable(LocalDateTime.now()) {


  private val logger = Logger(this.getClass)
  private implicit val ordering: Ordering[OrderBookEntry] = priority.ordering
  private var _orderId = side match {
    case Side.Bid => 0
    case Side.Ask => 1
  }

  private def getOrderID: Int = {
    val ret = _orderId
    _orderId += 2

    ret
  }

  private var activeOrders = mutable.TreeSet[OrderBookEntry]().++(orders)

  def getActiveOrders: mutable.TreeSet[OrderBookEntry] = {
    activeOrders
  }

  def submitOrder(order: Order): Option[List[Trade]] = {
    if (order.side == side && order.isInstanceOf[MarketOrder]
        || order.side != side && order.isInstanceOf[LimitOrder]) {
      throw new IllegalAccessError("Order side incompatible with this " + side)
    }

    order match {
      case LimitOrder(_, _, trader, price, size) =>
        addLimitOrder(trader, price, size)
        None
      case MarketOrder(_, _, trader, size) =>
        addMarketOrder(trader, size)
    }
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
  /**
    * @return Potential list of trades that occurred to filled this market order
    */
  def addMarketOrder(trader: Trader, size: Double): Option[List[Trade]] = {
    val iter = activeOrders.iterator
    var openOrder: OrderBookEntry = null
    var remainingSize = size
    var tradesThatHappened: List[Trade] = List[Trade]()
    while (iter.hasNext && remainingSize > 0) {
      openOrder = iter.next()
      val trade = reconcile(openOrder, trader, remainingSize)
      tradesThatHappened = tradesThatHappened ++ List(trade)
      activeOrders -= openOrder
      remainingSize -= openOrder.size
    }

    if (remainingSize > 0) {
      // We have run out of active orders on this side of the book, which is pretty bad news
      val errString = "0 orders remain on the " + side + " side"
      logger.error(errString)
      throw new IllegalStateException(errString)
    }

    if (remainingSize < 0) {
      // Re-add the partially matched open order to this OrderBookSide
      addLimitOrder(openOrder.trader, openOrder.price, -1 * remainingSize)
    }
    Some(tradesThatHappened)
  }

  protected[orderbook] def reconcile(openOrder: OrderBookEntry,
                                     trader: Trader,
                                     remainingSize: Double): Trade = {
    val taker = openOrder.trader
    val maker = trader
    val orderId = getOrderID
    val size = Math.min(remainingSize, openOrder.size)
    val trade = side match {
      case Side.Bid =>
        Trade(virtualTime,
              taker.id,
              openOrder.orderId,
              maker.id,
              orderId,
              openOrder.price,
              size)
      case Side.Ask =>
        Trade(virtualTime,
              maker.id,
              orderId,
              taker.id,
              openOrder.orderId,
              openOrder.price,
              size)
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

  private def getOrdersAtPrice(price: Double): Iterator[OrderBookEntry] = {
    activeOrders.filter(order => order.price == price).iterator
  }

  // TODO: consider accuracy of doubles...
  private def getDepth(price: Double): Double = {
    getOrdersAtPrice(price).map(_.size).sum
  }

  def cancelHead(): OrderBookEntry = {
    val cancelledOrder = activeOrders.head
    activeOrders -= activeOrders.head
    cancelledOrder
  }

  def cancelOrderAtPrice(targetPrice: Double): Option[OrderBookEntry] = {
    val iter = activeOrders.iterator
    var hasFoundOrder = false
    var order: OrderBookEntry = null

    while (iter.hasNext && !hasFoundOrder) {
      order = iter.next()
      hasFoundOrder = side match {
        case Side.Bid => order.price < targetPrice
        case Side.Ask => order.price > targetPrice
      }
    }

    if (hasFoundOrder) {
      order.trader.cancelOrder(order)
      activeOrders.remove(order)
      Some(order)
    } else {
      logger.debug("Couldn't find order that matched targetPrice: " + targetPrice)
      None
    }
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
