package simulator.order

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader

case class MarketOrder(override val time: LocalDateTime,
                       override val side: Side.Value,
                       override val trader: Trader,
                       size: Double)
    extends Order(time, side, trader) {
  override def toFields: Seq[String] = {
    List(time, side, trader.id, "NaN", size).map(o => o.toString)
  }

}
