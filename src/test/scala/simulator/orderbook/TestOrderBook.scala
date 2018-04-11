package simulator.orderbook

import simulator.order.Order

object TestOrderBook {
  def askSide = new OrderBookSide(OrderBookSideType.Ask)
  def bidSide = new OrderBookSide(OrderBookSideType.Bid)
  def getEmptyOrderBook = new OrderBook(askSide, bidSide)

  def getOrderBook(orders: List[Order]): OrderBook = {
    new OrderBook(askSide, bidSide, orders)
  }
}
