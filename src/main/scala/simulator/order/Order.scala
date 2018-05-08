package simulator.order

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader

// TODO: just parameterise this with a Buy/Sell/Cancel type?
abstract class Order(val time: LocalDateTime, val side: Side.Value, val trader: Trader) {
  def toFields: Seq[String]



}

