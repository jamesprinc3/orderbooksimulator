package simulator.orderbook

import simulator.order.{Order, OrderType, Trade}

object OrderBookFactory {

  def getOrderBook(orders: List[Order] = List()): OrderBook = {

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val orderBook = new OrderBook(askSide, bidSide, orders)

    orderBook
  }

}
