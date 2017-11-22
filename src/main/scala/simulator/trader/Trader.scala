package simulator.trader

import java.time.LocalDateTime

import simulator.order.Trade

// TODO: simulator.trader factory?
abstract class Trader(traderParams: TraderParams) {

  val id: Int = traderParams.id
  private var balance = traderParams.balance
  private var stock = traderParams.stock
  protected val orderBook = traderParams.orderBook

  def updateState(trade: Trade): Unit = {
    val diff = trade.price * trade.size
    if (trade.buyerId == id) {
      balance += diff
      stock += trade.size
    } else if (trade.sellerId == id) {
      balance -= diff
      stock -= trade.size
    }
  }

  // This way we can vary the size between steps, for optimisng TWAP stuff perhaps?
  def step(newTime: LocalDateTime)
}
