package mocks

import java.time.LocalDateTime

import simulator.Side
import simulator.events.OrderBookEntry
import simulator.order.Order
import simulator.orderbook.{OrderBook, OrderBookSide, TestOrderBook}

class MockOrderBook()
    extends OrderBook(TestOrderBook.askSide(new MockPriority(Side.Ask, 0)),
                      TestOrderBook.askSide(new MockPriority(Side.Bid, 0))) {

  override def getBidPrice: Double = { 0 }

  override def getBidSide: OrderBookSide = { null }

  override def getAskPrice: Double = { 0 }

  override def getAskSide: OrderBookSide = { null }

  override def getVolume: Double = { 0 }

  override def getPrice: Double = { 0 }

  override def submitOrder(order: Order, checkTime: Boolean = true, commitLog: Boolean = true): Unit = {}

  override def getOrder(orderId: Int): Option[OrderBookEntry] = {
    None
  }

  override def cancelOrder(orderId: Int): Boolean = {
    false
  }

  override def getNumberOfOrders: Int = {
    askSide.getActiveOrders.size + bidSide.getActiveOrders.size
  }

  override def getVolatility(ticks: Int): Double = {
    0
  }

  override def step(newTime: LocalDateTime): Unit = {}

}
