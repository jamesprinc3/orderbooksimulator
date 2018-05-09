package simulator.simulators

import java.time.LocalDateTime

import simulator.{Steppable, TransactionLog}
import simulator.orderbook.OrderBook
import simulator.trader.Trader

abstract class Simulator(startTime: LocalDateTime,
                         traders: List[Trader],
                         orderBooks: List[OrderBook])
    extends Steppable(startTime) {

//  protected var virtualTime: LocalDateTime = startTime

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
