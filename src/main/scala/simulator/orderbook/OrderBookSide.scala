package simulator.orderbook

import com.typesafe.scalalogging.Logger
import simulator.order.{OrderType, Trade}

import scala.collection.mutable

object OrderBookSideType extends Enumeration {
  val Bid, Ask = Value
}

class OrderBookSide(sideType: OrderBookSideType.Value, orders: List[OrderBookEntry] = List()) {

  private val logger = Logger(this.getClass)

  // TODO: match on simulator.order book type?
  implicit val ordering: Ordering[OrderBookEntry] = (x: OrderBookEntry, y: OrderBookEntry) => {
    var res = 0

    // Assume it's ask side first
    if (x.price == y.price) {
      if (x.arrivalTime.isBefore(y.arrivalTime)) {
        res = 1
      } else if (y.arrivalTime.isBefore(x.arrivalTime)){
        res = -1
      }
    } else if (x.price > y.price) {
      res = 1
    } else if (x.price < y.price) {
      res = -1
    }

    // But if it's Bid side, just reverse the sign
    if (sideType == OrderBookSideType.Bid) {
      res *= -1
    }

    if (x.orderId < y.orderId) {
      res = 1
    } else if (x.orderId > y.orderId) {
      res = -1
    }

    res
  }

  private var activeOrders = mutable.TreeSet[OrderBookEntry]().++(orders)

  def getActiveOrders: mutable.TreeSet[OrderBookEntry] = {
    activeOrders
  }

  // TODO: Error handling
  // TODO: is adding an order with the same ID as an existing order a fail?
  // TODO: split the order handling off into a different class?
  def addLimitOrder(order: OrderBookEntry): Unit = {
    // Need to check that if we're Bid side then we're getting a buy simulator.order here
    sideType match {
      case OrderBookSideType.Bid => if (order.orderType == OrderType.Sell) {
        logger.error("Expected simulator.order type Buy, but was Sell")
        return
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Buy) {
        logger.error("Expected simulator.order type Sell, but was Buy")
        return
      }
    }

    activeOrders.+=(order)
  }

  // TODO: Error handling
  // TODO: Do market orders just have a size, rather than a price?
  // TODO: Add an ID in here too? I think it makes sense to have that in the transaction log!
  /**
    * @param incomingOrder The order which we want to match on the market
    * @return None if the order was matched in its entirety.
    *         Some(order) if we were unable to match the simulator.order fully, in this situation the simulator.order book can
    *         choose whether to re-enter this as a limit simulator.order.
    */
  def addMarketOrder(incomingOrder: OrderBookEntry): (Option[List[Trade]], Option[OrderBookEntry]) = {
    // Bid side will accept a sell simulator.order here and vice versa
    sideType match {
      case OrderBookSideType.Bid => if (incomingOrder.orderType == OrderType.Buy) {
        logger.error("Expected simulator.order type Sell, but was Buy")
        return (None, Some(incomingOrder))
      }
      case OrderBookSideType.Ask => if (incomingOrder.orderType == OrderType.Sell) {
        logger.error("Expected simulator.order type Buy, but was Sell")
        return (None, Some(incomingOrder))
      }
    }

    val iter = activeOrders.iterator
    var remainingSize = incomingOrder.size
    var openOrder: OrderBookEntry = null
    var tradesThatHappened: List[Trade] = List[Trade]()
    while (iter.hasNext && remainingSize > 0) {
      openOrder = iter.next()
      var shouldMatch = false
      sideType match {
        case OrderBookSideType.Bid =>
          shouldMatch = openOrder.price >= incomingOrder.price
        case OrderBookSideType.Ask =>
          shouldMatch = openOrder.price <= incomingOrder.price
      }

      if (shouldMatch) {
        val trade = reconcile(openOrder, incomingOrder)
        tradesThatHappened = tradesThatHappened ++ List(trade)
        // TODO: perhaps move this logic into the reconcile function?
        activeOrders.remove(openOrder)
        remainingSize -= openOrder.size
      }
    }

    if (remainingSize > 0) {
      // Return the partially matched order (and the orderbook may reinstate it as a limit order on the other side of the book)
      val partialIncomingOrder = incomingOrder.copy(size = remainingSize)
      return (Some(tradesThatHappened), Some(partialIncomingOrder))
    }

    if (remainingSize < 0) {
      // Re-add the partially matched open order to this OrderBookSide
      val partialOpenOrder = openOrder.copy(size = -1*remainingSize)
      addLimitOrder(partialOpenOrder)
    }

    (Some(tradesThatHappened), None)
  }

  protected[orderbook] def reconcile(openOrder: OrderBookEntry, incomingOrder: OrderBookEntry): Trade = {
    val taker = openOrder.trader
    val maker = incomingOrder.trader
    val trade = sideType match {
      case OrderBookSideType.Bid =>
        // TODO: LocalDateTime needs to change to something more meaningful
        Trade(incomingOrder.arrivalTime, taker.id, openOrder.orderId,
            maker.id, incomingOrder.orderId, incomingOrder.price,
            Math.min(incomingOrder.size, openOrder.size))
      case OrderBookSideType.Ask =>
        Trade(incomingOrder.arrivalTime, maker.id, incomingOrder.orderId,
          taker.id, openOrder.orderId,incomingOrder.price,
          Math.min(incomingOrder.size, openOrder.size))
    }

    maker.updateState(trade)
    taker.updateState(trade)
    trade
  }

  private def getOrdersAtPrice(price: Double): Iterator[OrderBookEntry] = {
    activeOrders.filter(order => order.price == price).iterator
  }

  // TODO: consider accuracy of doubles...
  private def getDepth(price: Double): Double = {
    getOrdersAtPrice(price).map(_.size).sum
  }

  def cancelOrder(orderId: Int): Option[OrderBookEntry] = {
    val orderToCancel = activeOrders.find(order => order.orderId == orderId).getOrElse(return None)
    orderToCancel.trader.cancelOrder(orderToCancel)
    activeOrders.remove(orderToCancel)
    Some(orderToCancel)
  }

  def getBestPrice: Option[Double] = {
    val bestOrder = sideType match {
      case OrderBookSideType.Bid =>
        activeOrders.lastOption
      case OrderBookSideType.Ask =>
        activeOrders.headOption
    }

    if (bestOrder.isEmpty) {
      return None
    } else {
      return Some(bestOrder.get.price)
    }
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the simulator.order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
