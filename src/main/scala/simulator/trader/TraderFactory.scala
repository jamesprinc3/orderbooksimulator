package simulator.trader

class TraderFactory {

  /** The master trader is just an empty shell
    *
    */
  def getHandsOffTrader(): Trader = {
    val params = new TraderParams(0, 10, 0)
    new HandsOffTrader(params)
  }

}
