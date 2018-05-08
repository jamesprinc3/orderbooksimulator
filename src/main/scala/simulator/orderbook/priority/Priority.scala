package simulator.orderbook.priority

import simulator.Side
import simulator.events.OrderBookEntry

abstract class Priority(sixde: Side.Value) {

  implicit val ordering: Ordering[OrderBookEntry]

}
