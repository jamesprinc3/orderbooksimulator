package simulator.orderbook

import simulator.Side
import simulator.order.Order

object TestOrderBook {
  def askSide = new OrderBookSide(Side.Ask)
  def bidSide = new OrderBookSide(Side.Bid)
  def getEmptyOrderBook = new OrderBook(askSide, bidSide)

  def getOrderBook(orders: List[Order]): OrderBook = {
    new OrderBook(askSide, bidSide, orders)
  }
}
