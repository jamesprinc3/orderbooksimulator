package simulator.order

import java.time.LocalDateTime

import simulator.Side
import simulator.trader.Trader

case class LimitOrder(override val time: LocalDateTime,
                       override val side: Side.Value,
                      override val trader: Trader,
                      price: Double,
                      size: Double)
    extends Order(time, side, trader) {
  override def toFields: Seq[String] = {
    List(time, side, trader.id, price, size).map(o => o.toString)
  }
}
