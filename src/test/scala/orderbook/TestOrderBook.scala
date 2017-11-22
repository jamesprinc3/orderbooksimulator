package orderbook

import order.{Order, OrderType}

object TestOrderBook {
  def askSide = new OrderBookSide(OrderBookSideType.Ask)
  def bidSide = new OrderBookSide(OrderBookSideType.Bid)
  def getEmptyOrderBook = new OrderBook(askSide, bidSide)

  def getOrderBook(orders: List[Order]) = {
    val buys = orders.filter(order => order.orderType == OrderType.Buy)
    val sells = orders.filter(order => order.orderType == OrderType.Sell)

    val newBuySide = OrderBookSideHelper.getAskSide(buys)
    val newAskSide = OrderBookSideHelper.getAskSide(sells)

    new OrderBook(newAskSide, newBuySide)
  }
}
