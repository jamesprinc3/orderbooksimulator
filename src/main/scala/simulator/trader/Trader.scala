package simulator.trader

import java.time.LocalDateTime

import simulator.order.{Order, Trade}
import simulator.orderbook.OrderBook

// TODO: simulator.trader factory?
abstract class Trader(traderParams: TraderParams) {

  val id: Int = traderParams.id
  private var balance = traderParams.initialBalance
  private var holdings = traderParams.initialHoldings

  def updateState(trade: Trade): Unit = {
    val diff = trade.price * trade.size
    if (trade.buyerId == id) {
      balance += diff
      holdings += trade.size
    } else if (trade.sellerId == id) {
      balance -= diff
      holdings -= trade.size
    }
  }

  def step(newTime: LocalDateTime, orderBooks: List[OrderBook] = List())
    : List[(LocalDateTime, Trader, OrderBook, Order)]
}
