package simulator.order

import simulator.Side
import simulator.trader.Trader

case class LimitOrder(override val side: Side.Value,
                      override val trader: Trader,
                      price: Double,
                      size: Double)
    extends Order(side, trader)
