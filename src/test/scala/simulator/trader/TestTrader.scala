package simulator.trader
import java.time.LocalDateTime

import simulator.orderbook.OrderBook

class TestTrader(traderParams: TraderParams)
  extends Trader(traderParams) {

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook]): Unit = {}

}
