package simulator.simulators

import simulator.TransactionLog
import simulator.orderbook.OrderBook
import simulator.trader.Trader

abstract class Simulator(traders: List[Trader], orderBooks: List[OrderBook]) {

  def endCondition(): Boolean

  def updateState(): Unit

  def run(): Unit = {
    while (!endCondition()) {
      updateState()
    }
  }

  def getTransactionLogs: List[TransactionLog] = {
    orderBooks.map(_.transactionLog)
  }

}
