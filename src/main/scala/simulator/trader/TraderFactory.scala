package simulator.trader

object TraderFactory {

  /** The hands off trader is just an empty shell that does nothing
    *
    */
  def getHandsOffTrader: Trader = {
    val params = TraderParams(0, 10, 0)
    new HandsOffTrader(params)
  }

}
