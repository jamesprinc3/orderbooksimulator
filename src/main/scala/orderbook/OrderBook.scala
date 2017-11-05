package orderbook

import java.util

import order.{Order, OrderType}


class OrderBook(askSide: OrderBookSide, bidSide: OrderBookSide, orderQueue: util.Queue[Order]) {

  private var _orderId = 0
  private val tick = 1
  private val lot = 1

  def getBidPrice(): Unit = {

  }

  def getAskPrice(): Unit = {

  }

  private def getOrderID: Int = {
    _orderId += 1
    _orderId
  }

  def submitOrder(order: Order): Int = {
    order.orderType match {
      case OrderType.Buy => {
        submitBuyOrder(order)
        getOrderID
      }
      case OrderType.Sell => {
        submitSellOrder(order)
        getOrderID
      }
      case _ =>
        0
    }
  }

  private def submitBuyOrder(order: Order): Unit = {
    if (order.price >= askSide.getBestPrice()) {
      askSide.addMarketOrder(order)
    } else {
      bidSide.addLimitOrder(order)
    }
  }

  private def submitSellOrder(order: Order): Unit = {
    if (order.price <= bidSide.getBestPrice()) {
      bidSide.addMarketOrder(order)
    } else {
      askSide.addLimitOrder(order)
    }
  }

  def cancelOrder(orderId: Int): Boolean = {
    true
  }



}
