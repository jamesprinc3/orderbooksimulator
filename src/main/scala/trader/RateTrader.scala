package trader

import order.OrderType

// A Trader which submits orders at a given rate
// TODO: virtual clock?
// rate is mean orders per second
abstract class RateTrader(orderType: OrderType.Value,
                          rate: Int,
                          traderParams: TraderParams)
    extends Trader(traderParams) {

}
