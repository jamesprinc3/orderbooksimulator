package mocks

import java.time.LocalDateTime

import simulator.Side
import simulator.events.{OrderBookEntry, Trade}
import simulator.order.Order
import simulator.orderbook.OrderBook
import simulator.trader.{Trader, TraderParams}

class MockTrader extends Trader(traderParams = TraderParams(0, 0, 0)) {
  override def initialStep(orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = { List() }

  override def step(newTime: LocalDateTime, orderBooks: List[OrderBook])
    : List[(LocalDateTime, Trader, OrderBook, Order)] = { List() }

  override def updateState(trade: Trade): Unit = {}

  override def updateState(order: Order): Unit = {}

  override def updateState(order: OrderBookEntry): Unit = {}
}
