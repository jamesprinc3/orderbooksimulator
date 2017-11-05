package trader

import orderbook.OrderBook

// A Trader which submits orders at a given rate
class RateTrader(orderBook: OrderBook) extends Trader(orderBook) {

}
