package orderbook

import java.time.LocalDateTime

/**
  *
  * @param arrivalTime time in UTC
  */
case class OrderBookEntry(id: Int, arrivalTime: LocalDateTime, price: Int, size: Int)
