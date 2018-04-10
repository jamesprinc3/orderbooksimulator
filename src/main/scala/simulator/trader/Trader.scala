package simulator.trader

import java.time.LocalDateTime

import simulator.order.{Order, OrderType, Trade}
import simulator.orderbook.{OrderBook, OrderBookEntry}

// TODO: simulator.trader factory?
abstract class Trader(traderParams: TraderParams) {

  val id: Int = traderParams.id
  private var balance = traderParams.initialBalance
  private var holdings = traderParams.initialHoldings

  def updateState(trade: Trade): Unit = {
    val diff = trade.price * trade.size
    id match {
      case trade.buyerId =>
        balance -= diff
        holdings += trade.size

      case trade.sellerId =>
        balance += diff
        holdings -= trade.size

    }
  }

  def updateState(order: Order): Unit = {
    val diff = order.price * order.size
    order.orderType match {
      case OrderType.Buy =>
        balance -= diff
      case OrderType.Sell =>
        holdings -= order.size
    }
  }

  def cancelOrder(order: OrderBookEntry): Unit = {
    val diff = order.price * order.size
    order.orderType match {
      case OrderType.Buy =>
        balance += diff
      case OrderType.Sell =>
        holdings += order.size
    }
  }

  def getBalance: Double = {
    balance
  }

  def getHoldings: Double = {
    holdings
  }

  def step(newTime: LocalDateTime, orderBooks: List[OrderBook] = List())
    : List[(LocalDateTime, Trader, OrderBook, Order)]
}
