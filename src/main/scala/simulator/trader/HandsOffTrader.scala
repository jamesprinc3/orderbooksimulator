package simulator.trader

import java.time.LocalDateTime

import simulator.orderbook.OrderBook

/** The hands off trader does not participate in the market
  */
class HandsOffTrader(traderParams: TraderParams)
  extends Trader(traderParams) {

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook]): Unit = {}
}