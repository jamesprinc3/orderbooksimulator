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
  def addMarketOrder(order: Order): Unit = {
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
      // TODO: partial matching of orders
      var remainingSize = order.size
      getOrdersAtPrice(order.price).foreach(activeOrder => {
        if (remainingSize - activeOrder.size >= 0) {
          remainingSize -= activeOrder.size
        } else {
          activeOrder.copy(size = (activeOrder - remainingSize))
        }

      })
    } else {
      // TODO: partial filling of orders
      println("Not enough depth to complete order")
      return
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

  def getBestPrice(): Int = {
    0
  }

  // TODO: calculate some metrics (as outlined in the Gould paper for this side of the order book here, or maybe that should be moved out to another class? e.g. OrderBookMetrics

}
