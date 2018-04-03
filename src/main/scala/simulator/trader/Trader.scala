package simulator.trader

import java.time.LocalDateTime

import simulator.order.Trade
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

  // This way we can vary the size between steps, for optimisng TWAP stuff perhaps?
  def step(newTime: LocalDateTime, orderBooks: List[OrderBook] = List())
}
