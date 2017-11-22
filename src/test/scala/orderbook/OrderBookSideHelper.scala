package orderbook

import java.time.LocalDateTime

import order.Order

object OrderBookSideHelper {
  // TODO: unify these two methods?
  def getAskSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(OrderBookSideType.Ask, orderBookEntries)
  }

  def getBidSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(OrderBookSideType.Bid, orderBookEntries)
  }

  private def getOrderBookEntries(orders: List[Order]) = {
    orders.indices.map(x => {
      val order = orders(x)
      OrderBookEntry(null, x, LocalDateTime.now(), order.price, order.size)
    }).toList
  }
}
