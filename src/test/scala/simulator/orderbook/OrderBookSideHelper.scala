package simulator.orderbook

import java.time.LocalDateTime

import mocks.MockPriority
import org.scalamock.scalatest.MockFactory
import simulator.Side
import simulator.events.OrderBookEntry
import simulator.order.LimitOrder
import simulator.orderbook.priority.PriceSize

object OrderBookSideHelper extends MockFactory {

  private val buyPriority = new PriceSize(Side.Bid)
  private val sellPriority = new PriceSize(Side.Bid)

  // TODO: unify these two methods?
  def getAskSide(orders: List[LimitOrder] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(Side.Ask, sellPriority, orderBookEntries)
  }

  def getBidSide(orders: List[LimitOrder] = List()): OrderBookSide = {
    val orderBookEntries = getOrderBookEntries(orders)
    new OrderBookSide(Side.Bid, buyPriority, orderBookEntries)
  }

  private def getOrderBookEntries(orders: List[LimitOrder]) = {
    var index = 0

    orders.map {
      case LimitOrder(time, side, trader, price, size) =>
        index += 1
        OrderBookEntry(side, trader, index, LocalDateTime.now(), price, size)
    }
  }
}
