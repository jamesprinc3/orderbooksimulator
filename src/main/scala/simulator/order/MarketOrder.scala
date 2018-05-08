package simulator.order

import simulator.Side
import simulator.trader.Trader

case class MarketOrder(override val side: Side.Value,
                       override val trader: Trader,
                       size: Double)
    extends Order(side, trader)
