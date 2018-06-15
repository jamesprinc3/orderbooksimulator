package simulator.order

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader

abstract class Order(val time: LocalDateTime, val side: Side.Value, val trader: Trader) {
  def toFields: Seq[String]



}

