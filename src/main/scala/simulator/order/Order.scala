package simulator.order

import simulator.Side
import simulator.trader.Trader

// TODO: just parameterise this with a Buy/Sell/Cancel type?
abstract class Order(val side: Side.Value, val trader: Trader)

