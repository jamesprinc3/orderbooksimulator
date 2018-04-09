package simulator.orderbook

import java.time.LocalDateTime

import simulator.trader.Trader
import simulator.order.OrderType

/**
  * @param arrivalTime time in UTC
  */
case class OrderBookEntry(orderType: OrderType.Value, trader: Trader, orderId: Int, arrivalTime: LocalDateTime, price: Int, size: Int) {
  override def toString: String = {
    "ORDER " + orderType +
    " trader: " + trader.id +
    " orderId: " +  orderId +
    " time: " + arrivalTime +
    " price: " + price +
    " size: " + size
  }
}
