package orderbook

import java.time.LocalDateTime

/**
  *
  * @param arrivalTime time in UTC
  */
case class OrderBookEntry(price: Int, arrivalTime: LocalDateTime, size: Int)
