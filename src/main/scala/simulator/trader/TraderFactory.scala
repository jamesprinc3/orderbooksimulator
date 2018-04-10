package simulator.trader

import java.time.LocalDateTime

import simulator.order.OrderType

object TraderFactory {
  def getBasicTraders() = {
    val startTime = LocalDateTime.now()

    val buyParams = TraderParams(17, 100, 1)
    val sellParams = TraderParams(18, 100, 1)

    val buyTrader = new BestPriceRateTrader(OrderType.Buy, 100, startTime, buyParams)
    val sellTrader = new BestPriceRateTrader(OrderType.Sell, 100, startTime, sellParams)

    List(buyTrader, sellTrader)
  }


  /** The hands off trader is just an empty shell that does nothing
    *
    */
  def getHandsOffTrader: Trader = {
    val params = TraderParams(0, 10, 0)
    new HandsOffTrader(params)
  }

}
