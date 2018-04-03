package simulator.orderbook

import java.time.LocalDateTime

import simulator.TradeLog
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
    * @param incomingOrder The order which we want to match on the market
    * @return None if the order was matched in its entirety.
    *         Some(order) if we were unable to match the simulator.order fully, in this situation the simulator.order book can
    *         choose whether to re-enter this as a limit simulator.order.
    */
  def addMarketOrder(trader: Trader, incomingOrder: Order, incomingOrderId:Int): (Option[List[Trade]], Option[Order]) = {
    // Bid side will accept a sell simulator.order here
    sideType match {
      case OrderBookSideType.Bid => if (incomingOrder.orderType == OrderType.Buy) {
        println("Expected simulator.order type Sell, but was Buy")
        return (None, Some(incomingOrder))
      }
      case OrderBookSideType.Ask => if (incomingOrder.orderType == OrderType.Sell) {
        println("Expected simulator.order type Buy, but was Sell")
        return (None, Some(incomingOrder))
      }
    }

    val depthAtPrice = getDepth(incomingOrder.price)
    if (depthAtPrice >= 0) {
      var remainingSize = incomingOrder.size
      val iter = activeOrders.iterator
      var openOrder: OrderBookEntry = null
      var tradesThatHappened: List[Trade] = List[Trade]()
      while (remainingSize > 0 && iter.hasNext) {
        openOrder = iter.next()
        remainingSize -= openOrder.size
        activeOrders.remove(openOrder)

        val trade = reconcile(trader, openOrder, incomingOrder, incomingOrderId)
        tradesThatHappened = tradesThatHappened ++ List(trade)
        // TODO: the above line is pretty ugly!
      }

      if (remainingSize > 0) {
        // Enter this partially matched order as a limit simulator.order (on the other side of the book!)
        val partialIncomingOrder = incomingOrder.copy(size = remainingSize)
        return (Some(tradesThatHappened), Some(partialIncomingOrder))
      }

      if (remainingSize < 0) {
        // Put this partially matched order back in our active orders as a limit order
        val partialOpenOrder = openOrder.copy(size = -1*remainingSize)
        activeOrders.+=(partialOpenOrder)
      }
      return (Some(tradesThatHappened), None)
    }
    (None, Some(incomingOrder))
  }

  protected[orderbook] def reconcile(maker: Trader, openOrder: OrderBookEntry, incomingOrder: Order, incomingOrderId: Int): Trade = {
    val taker = openOrder.trader
    val trade = sideType match {
      case OrderBookSideType.Bid =>
        // TODO: LocalDateTime needs to change to something more meaningful
        Trade(LocalDateTime.now(), taker.id, openOrder.orderId,
            maker.id, incomingOrderId, incomingOrder.price,
            Math.min(incomingOrder.size, openOrder.size))
      case OrderBookSideType.Ask =>
        Trade(LocalDateTime.now(), maker.id, incomingOrderId,
          taker.id, openOrder.orderId,incomingOrder.price,
          Math.min(incomingOrder.size, openOrder.size))
    }
    // TODO: submit the trade to some kind of transaction log

    maker.updateState(trade)
    taker.updateState(trade)
    trade
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
