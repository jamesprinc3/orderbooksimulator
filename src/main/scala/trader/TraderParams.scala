package trader

import orderbook.OrderBook

case class TraderParams(orderBook: OrderBook, id: Int, balance: Int, stock: Int)
