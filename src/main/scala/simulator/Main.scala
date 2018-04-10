package simulator

import java.time.LocalDateTime

import simulator.order.{Order, OrderType}
import simulator.orderbook.{OrderBook, OrderBookFactory, OrderBookSide, OrderBookSideType}
import simulator.trader.BestPriceRateTrader
import simulator.trader.TraderParams

import scala.concurrent.duration.Duration

object Main {

  def main(args: Array[String]): Unit = {
    println("Hello world!")

    val startTime = LocalDateTime.now()

    val bestBuyPrice = 99
    val bestSellPrice = 101
    val standardOrderSize = 1
    val basicBuyOrder = Order(OrderType.Buy, bestBuyPrice, standardOrderSize)
    val basicSellOrder = Order(OrderType.Sell, bestSellPrice, standardOrderSize)

    val buyParams = TraderParams(17, 100, 1)
    val sellParams = TraderParams(18, 100, 1)

    val buyTrader = new BestPriceRateTrader(OrderType.Buy, 100, startTime, buyParams)
    val sellTrader = new BestPriceRateTrader(OrderType.Sell, 100, startTime, sellParams)

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val transactionLog = new TransactionLog()
//    val orderBook = new OrderBook(askSide, bidSide, List(), transactionLog)
    val orderBook = OrderBookFactory.getPopulatedOrderBook(20)

//    orderBook.submitOrder(sellTrader, basicSellOrder)
    //    orderBook.submitOrder(buyTrader, basicBuyOrder)

    val simulator = new Simulator(LocalDateTime.now(),
      Duration.fromNanos(1e6),
      20,
      List(buyTrader, sellTrader),
      List(orderBook))

    simulator.run()
    println(orderBook.transactionLog.toString)

    println("Simulation finished")


  }

//  def startSimulation(): Unit = {
//    val trader = BestPriceRateTrader
//  }


}
