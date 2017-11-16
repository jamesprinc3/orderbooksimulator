package trader

import order.Trade

// TODO: trader factory?
class Trader(traderParams: TraderParams) {

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
}
