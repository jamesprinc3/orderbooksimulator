package simulator.orderbook

import java.time.LocalDateTime

import org.scalamock.scalatest.MockFactory
import simulator.Side
import simulator.order.Order
import simulator.orderbook.priority.Priority

object TestOrderBook extends MockFactory {
  def askSide(priority: Priority) = new OrderBookSide(Side.Ask, priority)
  def bidSide(priority: Priority) = new OrderBookSide(Side.Bid, priority)
  def getEmptyOrderBook(
      askPriority: Priority,
      BidPriority: Priority,
      startTime: LocalDateTime = LocalDateTime.now()): OrderBook =
    new OrderBook(askSide(askPriority), bidSide(BidPriority), startTime = startTime)

  def getOrderBook(
      orders: List[Order],
      priority: Priority,
      startTime: LocalDateTime = LocalDateTime.now()): OrderBook = {
    new OrderBook(askSide(priority),
                  bidSide(priority),
                  orders,
                  startTime = startTime)
  }
}
