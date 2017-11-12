package orderbook

import java.time.LocalDateTime

import order.{Order, OrderType}

import scala.collection.mutable

object OrderBookSideType extends Enumeration {
  val Bid, Ask = Value
}

class OrderBookSide(sideType: OrderBookSideType.Value, orders: List[OrderBookEntry] = List()) {

  // TODO: match on order book type?
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

    if (x.id == y.id) {
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
  def addLimitOrder(order: Order, id: Int): Unit = {
    // Need to check that if we're Bid side then we're getting a buy order here
    sideType match {
      case OrderBookSideType.Bid => if (order.orderType == OrderType.Sell) {
        println("Expected order type Buy, but was Sell")
        return
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Buy) {
        println("Expected order type Sell, but was Buy")
        return
      }
    }

    val orderBookEntry = OrderBookEntry(id, LocalDateTime.now(), order.price, order.size)

    activeOrders.+=(orderBookEntry)
  }

  // TODO: Error handling
  // TODO: Do market orders just have a size, rather than a price?
  // TODO: This function is fairly mucky, use generics instead
  /**
    * @param order The order which we want to match on the market
    * @return None if the order was matched in its entirety.
    *         Some(order) if we were unable to match the order fully, in this situation the order book can
    *         choose whether to re-enter this as a limit order.
    */
  def addMarketOrder(order: Order): Option[Order] = {
    // Bid side will accept a sell order here
    sideType match {
      case OrderBookSideType.Bid => if (order.orderType == OrderType.Buy) {
        println("Expected order type Sell, but was Buy")
        return Some(order)
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Sell) {
        println("Expected order type Buy, but was Sell")
        return Some(order)
      }
    }

    val depthAtPrice = getDepth(order.price)
    if (depthAtPrice >= order.size) {
      var remainingSize = order.size
      val iter = activeOrders.iterator
      var activeOrder: OrderBookEntry = null
      while (remainingSize > 0 && iter.hasNext) {
        activeOrder = iter.next()
        remainingSize -= activeOrder.size
        activeOrders.remove(activeOrder)
      }

      if (remainingSize > 0) {
        // Enter this partially matched order as a limit order (on the other side of the book!)
        val partialIncomingOrder = order.copy(size = remainingSize)
        return Some(partialIncomingOrder)
      }

      if (remainingSize < 0) {
        // Put this partially matched order back in our active orders as a limit order
        val partialActiveOrder = activeOrder.copy(size = -1*remainingSize)
        activeOrders.+=(partialActiveOrder)
      }
    }
    None
  }

  private def getOrdersAtPrice(price: Int): Iterator[OrderBookEntry] = {
    activeOrders.filter(order => order.price == price).iterator
  }

  private def getDepth(price: Int): Int = {
    getOrdersAtPrice(price).map(_.price).sum
  }

  def cancelOrder(orderId: Int): Option[OrderBookEntry] = {
    val orderToCancel = activeOrders.find(order => order.id == orderId).getOrElse(return None)
    activeOrders.remove(orderToCancel)
    Some(orderToCancel)
  }

  def getBestPrice: Int = {
    activeOrders.head.price
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
