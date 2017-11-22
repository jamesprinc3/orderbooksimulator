package simulator.orderbook

import java.time.LocalDateTime

import simulator.order.{Order, OrderType, Trade}
import simulator.trader.Trader

import scala.collection.mutable

object OrderBookSideType extends Enumeration {
  val Bid, Ask = Value
}

class OrderBookSide(sideType: OrderBookSideType.Value, orders: List[OrderBookEntry] = List()) {

  // TODO: match on simulator.order book type?
  implicit val ordering: Ordering[OrderBookEntry] = (x: OrderBookEntry, y: OrderBookEntry) => {
    var res = 0

    // Assume it's ask side first
    if (x.price == y.price) {
      if (x.arrivalTime.isBefore(y.arrivalTime)) {
        res = 1
      } else {
        res = -1
      }
    } else if (x.price > y.price) {
      res = 1
    } else {
      res = -1
    }

    // But if it's Bid side, just reverse the sign
    if (sideType == OrderBookSideType.Bid) {
      res *= -1
    }

    if (x.orderId == y.orderId) {
      res = 0
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
  def addLimitOrder(trader: Trader, order: Order, id: Int): Unit = {
    // Need to check that if we're Bid side then we're getting a buy simulator.order here
    sideType match {
      case OrderBookSideType.Bid => if (order.orderType == OrderType.Sell) {
        println("Expected simulator.order type Buy, but was Sell")
        return
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Buy) {
        println("Expected simulator.order type Sell, but was Buy")
        return
      }
    }

    val orderBookEntry = OrderBookEntry(trader, id, LocalDateTime.now(), order.price, order.size)

    activeOrders.+=(orderBookEntry)
  }

  // TODO: Error handling
  // TODO: Do market orders just have a size, rather than a price?
  // TODO: Add an ID in here too? I think it makes sense to have that in the transaction log!
  /**
    * @param order The sorder which we want to match on the market
    * @return None if the order was matched in its entirety.
    *         Some(order) if we were unable to match the simulator.order fully, in this situation the simulator.order book can
    *         choose whether to re-enter this as a limit simulator.order.
    */
  def addMarketOrder(trader: Trader, order: Order): Option[Order] = {
    // Bid side will accept a sell simulator.order here
    sideType match {
      case OrderBookSideType.Bid => if (order.orderType == OrderType.Buy) {
        println("Expected simulator.order type Sell, but was Buy")
        return Some(order)
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Sell) {
        println("Expected simulator.order type Buy, but was Sell")
        return Some(order)
      }
    }

    val depthAtPrice = getDepth(order.price)
    if (depthAtPrice >= 0) {
      var remainingSize = order.size
      val iter = activeOrders.iterator
      var activeOrder: OrderBookEntry = null
      while (remainingSize > 0 && iter.hasNext) {
        activeOrder = iter.next()
        remainingSize -= activeOrder.size
        activeOrders.remove(activeOrder)

        reconcile(trader, activeOrder)
      }

      if (remainingSize > 0) {
        // Enter this partially matched order as a limit simulator.order (on the other side of the book!)
        val partialIncomingOrder = order.copy(size = remainingSize)
        return Some(partialIncomingOrder)
      }

      if (remainingSize < 0) {
        // Put this partially matched order back in our active orders as a limit order
        val partialActiveOrder = activeOrder.copy(size = -1*remainingSize)
        activeOrders.+=(partialActiveOrder)
      }
      return None
    }
    Some(order)
  }

  protected[orderbook] def reconcile(maker: Trader, activeOrder: OrderBookEntry): Unit = {
    val taker = activeOrder.trader
    val trade = sideType match {
      case OrderBookSideType.Bid =>
        Trade(taker.id, maker.id, activeOrder.price, activeOrder.size)
      case OrderBookSideType.Ask =>
        Trade(maker.id, taker.id, activeOrder.price, activeOrder.size)
    }
    // TODO: submit the trade to some kind of transaction log

    maker.updateState(trade)
    taker.updateState(trade)
  }

  private def getOrdersAtPrice(price: Int): Iterator[OrderBookEntry] = {
    activeOrders.filter(order => order.price == price).iterator
  }

  private def getDepth(price: Int): Int = {
    getOrdersAtPrice(price).map(_.size).sum
  }

  def cancelOrder(orderId: Int): Option[OrderBookEntry] = {
    val orderToCancel = activeOrders.find(order => order.orderId == orderId).getOrElse(return None)
    activeOrders.remove(orderToCancel)
    Some(orderToCancel)
  }

  def getBestPrice: Option[Int] = {
    val firstOrder = activeOrders.headOption.getOrElse(return None)
    Some(firstOrder.price)
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the simulator.order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
