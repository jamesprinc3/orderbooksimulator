package simulator.orderbook

import java.time.LocalDateTime

import mocks.MockTrader
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import simulator.Side
import simulator.order.LimitOrder
import simulator.trader.{Trader, TraderParams}

class OrderBookFactorySpec extends FlatSpec with MockFactory {
  private val buyPrice = 9
  private val sellPrice = 11

  abstract class TraderExt extends Trader(TraderParams(0, 0, 0))

  private def mockTrader = new MockTrader()

  def buyOrder =
    LimitOrder(LocalDateTime.now(), Side.Bid, mockTrader, buyPrice, 10)
  def sellOrder =
    LimitOrder(LocalDateTime.now(), Side.Ask, mockTrader, sellPrice, 10)

  private val buyOrders = Range(0, 5).map(_ => buyOrder).toList
  private val sellOrders = Range(0, 5).map(_ => sellOrder).toList

  "getOrderBook" should "give empty OrderBook when given no orders" in {
    val orderBook = OrderBookFactory.getOrderBook()

    assert(orderBook.getNumberOfOrders == 0)
  }

  it should "give correct number of orders in OrderBook when given one buy order" in {
    val orderBook = OrderBookFactory.getOrderBook(List(buyOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given one sell order" in {
    val orderBook = OrderBookFactory.getOrderBook(List(sellOrder))

    assert(orderBook.getNumberOfOrders == 1)
  }

  it should "give correct number of orders in OrderBook when given multiple buy orders" in {
    val orderBook = OrderBookFactory.getOrderBook(buyOrders)

    assert(orderBook.getNumberOfOrders == 5)
  }

  it should "give correct number of orders in OrderBook when given multiple sell orders" in {
    val orderBook = OrderBookFactory.getOrderBook(sellOrders)

    assert(orderBook.getNumberOfOrders == 5)
  }

  it should "give correct number of orders in OrderBook when given mix of orders" in {
    val orderBook = OrderBookFactory.getOrderBook(buyOrders ++ sellOrders)

    assert(orderBook.getNumberOfOrders == 10)
  }

}
