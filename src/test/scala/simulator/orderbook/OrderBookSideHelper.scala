package simulator.orderbook

import java.time.LocalDateTime

import simulator.events.OrderBookEntry
import simulator.{Side, TestConstants}
import simulator.order.Order

object OrderBookSideHelper {
  // TODO: unify these two methods?
  def getAskSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(Side.Ask, orderBookEntries)
  }

  def getBidSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(Side.Bid, orderBookEntries)
  }

  private def getOrderBookEntries(orders: List[Order]) = {
    orders.indices.map(x => {
      val order = orders(x)
      OrderBookEntry(order.orderType, null, TestConstants.minOrderIndex + x, LocalDateTime.now(), order.price, order.size)
    }).toList
  }
}
