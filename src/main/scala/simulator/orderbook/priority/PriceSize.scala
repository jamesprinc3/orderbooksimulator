package simulator.orderbook.priority

import simulator.Side
import simulator.events.OrderBookEntry

class PriceSize(side: Side.Value) extends Priority(side) {
  implicit val ordering: Ordering[OrderBookEntry] =
    (x: OrderBookEntry, y: OrderBookEntry) => {
      var res = 0

      // Assume it's ask side first
      if (x.price == y.price) {
        if (x.time.isBefore(y.time)) {
          res = 1
        } else if (y.time.isBefore(x.time)) {
          res = -1
        }
      } else if (x.price > y.price) {
        res = 1
      } else if (x.price < y.price) {
        res = -1
      }

      // But if it's Bid side, just reverse the sign
      if (side == Side.Bid) {
        res *= -1
      }

      if (res == 0) {
        if (x.orderId < y.orderId) {
          res = 1
        } else if (x.orderId > y.orderId) {
          res = -1
        }
      }

      res
    }
}
