package trader

import orderbook.OrderBook

// TODO: make this into a list of orderBooks
case class TraderParams(orderBook: OrderBook, id: Int, balance: Int, stock: Int)
