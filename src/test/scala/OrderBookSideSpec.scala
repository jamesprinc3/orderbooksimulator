import java.time.LocalDateTime

import order.{Order, OrderType}
import orderbook.{OrderBookEntry, OrderBookSide, OrderBookSideType}
import org.scalatest._

class OrderBookSideSpec extends FlatSpec {

  def getOrderBookSide(orders: List[Order] = List()): OrderBookSide = {
    val orderBookEntries = orders.indices.map(x => {
      val order = orders(x)
      // TODO: encode this logic in just one place?
      OrderBookEntry(x, LocalDateTime.now(), order.price, order.size)
    }).toList
    new OrderBookSide(OrderBookSideType.Bid, orderBookEntries)
  }

  val bestPrice = 100
  val order = Order(OrderType.Buy, bestPrice, 10)
  def singularOrderBookSide: OrderBookSide = getOrderBookSide(List(order))

  "getBestPrice" should "display the best price with one order" in {
    assert(singularOrderBookSide.getBestPrice == bestPrice)
  }

  it should "display the best price with many orders of the same price" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice, 10)
    }).toList
    val orderBookSide = getOrderBookSide(orders)

    assert(orderBookSide.getBestPrice == bestPrice)
  }

  it should "display the best price with many orders of different prices" in {
    val bestPrice = 100
    val orders = Range(0,3).map(x => {
      Order(OrderType.Buy, bestPrice-x, 10)
    }).toList
    val orderBookSide = getOrderBookSide(orders)

    assert(orderBookSide.getBestPrice == bestPrice)
  }

  "cancelOrder" should "remove only active order when given a correct id" in {
    val orderBookSide = singularOrderBookSide
    orderBookSide.cancelOrder(0)

    assert(orderBookSide.getActiveOrders.toList.isEmpty)
  }

  "cancelOrder" should "return an order from active orders when given a correct id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(0).isDefined)
  }

  "cancelOrder" should "return a partially filled order from active orders when given a correct id" in {
    // TODO: this, requires generating orders of the other type
    true
  }

  it should "return None when given an incorrect id" in {
    val orderBookSide = singularOrderBookSide

    assert(orderBookSide.cancelOrder(1).isEmpty)
  }
}
