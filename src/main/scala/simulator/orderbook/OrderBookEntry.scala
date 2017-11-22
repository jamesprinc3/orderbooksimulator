package simulator.orderbook

import java.time.LocalDateTime

import simulator.trader.Trader

/**
  * @param arrivalTime time in UTC
  */
case class OrderBookEntry(trader: Trader, orderId: Int, arrivalTime: LocalDateTime, price: Int, size: Int)
