package trader

import java.time.LocalDateTime

import orderbook.OrderBook
import order.OrderType

// A Trader which submits orders at a given rate
// TODO: virtual clock?
// rate is mean orders per second
abstract class RateTrader(orderType: OrderType.Value,
                          rate: Int,
                          traderParams: TraderParams)
    extends Trader(traderParams) {

  // This way we can vary the size between steps, for optimisng TWAP stuff perhaps?
  def step(newTime: LocalDateTime)

}
