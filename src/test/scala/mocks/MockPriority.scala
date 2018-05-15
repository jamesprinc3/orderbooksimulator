package mocks

import simulator.Side
import simulator.events.OrderBookEntry
import simulator.orderbook.priority.Priority

class MockPriority(side: Side.Value, ret: Int) extends Priority(side) {
  implicit val ordering: Ordering[OrderBookEntry] =
    (_, _) => {
      ret
    }
}
