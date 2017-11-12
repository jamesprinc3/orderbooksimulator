import order.{Order, OrderType}
import orderbook.{OrderBookSide, OrderBookSideType}
import org.scalatest._

class OrderBookSideSpec extends FlatSpec {

  def getOrderBookSide(): OrderBookSide = {
    new OrderBookSide(OrderBookSideType.Bid)
  }

  "getBestPrice" should "display the best price with one order" in {
    val orderBookSide = getOrderBookSide()
    val bestPrice = 100
    val order = Order(OrderType.Buy, bestPrice, 10)
    orderBookSide.addLimitOrder(order, 1)

    assert(orderBookSide.getBestPrice == bestPrice)
  }
}
