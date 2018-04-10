package simulator.trader
import java.time.LocalDateTime

import simulator.order.Order
import simulator.orderbook.OrderBook

class TestTrader(traderParams: TraderParams) extends Trader(traderParams) {

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = {List()}

}
