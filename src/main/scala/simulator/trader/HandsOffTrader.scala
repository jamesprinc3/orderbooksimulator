package simulator.trader

import java.time.LocalDateTime

import simulator.order.Order
import simulator.orderbook.OrderBook

/** The hands off trader inserts orders into the order book
  * before the simulation starts and then does not participate in the market
  */
class HandsOffTrader(traderParams: TraderParams) extends Trader(traderParams) {

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = {List()}
}
