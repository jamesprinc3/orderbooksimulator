package simulator.simulators

import simulator.TransactionLog
import simulator.orderbook.OrderBook
import simulator.trader.Trader

abstract class Simulator(traders: List[Trader], orderBooks: List[OrderBook]) {

  def endCondition(): Boolean

  def updateState(): Unit

  def initialState(): Unit

  def run(): Unit = {
    val t0 = System.nanoTime()

    initialState()
    while (!endCondition()) {
      updateState()
    }


  }

  def getTransactionLogs: List[TransactionLog] = {
    orderBooks.map(_.transactionLog)
  }

}
