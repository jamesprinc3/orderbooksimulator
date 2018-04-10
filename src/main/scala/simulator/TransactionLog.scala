package simulator

import simulator.order.Trade
import simulator.orderbook.OrderBookEntry

class TransactionLog() {

  var trades: List[Trade] = List()
  var orders: List[OrderBookEntry] = List()

  // TODO: maybe this is dog-slow?
  def addTrade(trade: Trade): Unit = {
    trades = trades ::: List(trade)
  }

  def addOrder(order: OrderBookEntry): Unit = {
    orders = orders ::: List(order)
  }

  def writeToFile(filepath: String): Unit = {
    
  }

  override def toString: String = {
    var res = "Orders made:\n"
    res += orders.map(_.toString).mkString("\n") + "\n\n"

    res += "Trades made:\n"
    res += trades.map(_.toString).mkString("\n")

    res
  }
}
