package trader

import java.time.LocalDateTime

import orderbook.OrderBook
import order.OrderType

// A Trader which submits orders at a given rate
// TODO: virtual clock?
// rate is mean orders per second
abstract class RateTrader(orderBook: OrderBook, orderType: OrderType.Value, rate: Int) extends Trader(orderBook) {

  // This way we can vary the tick size, for optimisng TWAP stuff perhaps?
  def step(newTime: LocalDateTime)

}
