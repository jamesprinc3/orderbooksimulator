package simulator

import simulator.order.Trade

class TradeLog {
  var log: List[Trade] = List()

  // TODO: maybe this is dog-slow?
  def addTrade(trade: Trade): Unit = {
    log = log ::: List(trade)
  }

  override def toString: String = {
    log.map(trade => trade.toString).mkString("\n")
  }
}
