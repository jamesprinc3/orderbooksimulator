package simulator.simulators

import java.time.LocalDateTime

import simulator.logs.OrderBookLog
import simulator.orderbook.OrderBook
import simulator.trader.Trader
import simulator.traits.Steppable

abstract class OrderBookSimulator(startTime: LocalDateTime,
                                  traders: List[Trader],
                                  orderBooks: List[OrderBook]) extends Steppable(startTime) with Simulator {

  def getTransactionLogs: List[OrderBookLog] = {
    orderBooks.map(_.orderBookLog)
  }

}
