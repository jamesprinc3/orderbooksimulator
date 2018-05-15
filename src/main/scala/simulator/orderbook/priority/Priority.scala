package simulator.orderbook.priority

import simulator.Side
import simulator.events.OrderBookEntry

abstract class Priority(side: Side.Value) {

  implicit val ordering: Ordering[OrderBookEntry]

}
