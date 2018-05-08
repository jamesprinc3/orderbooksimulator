package simulator.trader

import simulator.Side

// TODO: virtual clock?
/** A Trader which submits orders at a given rate
  * @param orderType the type of order this trader will produce
  * @param rate mean orders per second
  * @param traderParams parameters to pass to parent class
  */
abstract class RateTrader(orderType: Side.Value,
                          rate: Int,
                          traderParams: TraderParams)
    extends Trader(traderParams) {

}
