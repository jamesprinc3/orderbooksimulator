package orderbook

import java.time.LocalDateTime

import trader.Trader

/**
  * @param arrivalTime time in UTC
  */
// TODO: add trader id
case class OrderBookEntry(trader: Trader, orderId: Int, arrivalTime: LocalDateTime, price: Int, size: Int)
