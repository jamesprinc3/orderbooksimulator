package orderbook

import java.time.LocalDateTime

import order.{Order, OrderType}

import scala.collection.mutable.TreeSet

object OrderBookSideType extends Enumeration {
  val Bid, Ask = Value
}

class OrderBookSide(sideType: OrderBookSideType.Value) {

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

    res
  }



  private val activeOrders = TreeSet[OrderBookEntry]()

  // TODO: Error handling
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
  // TODO: This function is fairly mucky
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
        return
      }
      case OrderBookSideType.Ask => if (order.orderType == OrderType.Sell) {
        println("Expected order type Buy, but was Sell")
        return
      }
    }

    val depthAtPrice = getDepth(order.price)
    if (depthAtPrice >= order.size) {
      var remainingSize = order.size
      val iter = activeOrders.iterator
      var activeOrder: OrderBookEntry = _
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

  }

  private def getOrdersAtPrice(price: Int) = {
    activeOrders.filter(order => order.price == price)
  }

  private def getDepth(price: Int) = {
    getOrdersAtPrice(price).reduce((order1, order2) => order1.price + order2.price)
  }

  def cancelOrder(orderId: Int): Boolean = {
    activeOrders.filter(order => order.id != orderId)
  }

  def getBestPrice: Int = {
    activeOrders.head.price
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
