package simulator

import java.time.LocalDateTime

import simulator.order.{Order, OrderType}
import simulator.orderbook.{
  OrderBook,
  OrderBookFactory,
  OrderBookSide,
  OrderBookSideType
}
import simulator.simulators.{DiscreteEventSimulator, TimeSliceSimulator}
import simulator.trader.{BestPriceRateTrader, TraderFactory, TraderParams}

import scala.concurrent.duration.Duration

object Main {

  val filePath = "/Users/jamesprince/project-data/orderbook.csv"

  def main(args: Array[String]): Unit = {
    val traders = TraderFactory.getRandomTraders(0.1, 100)
    val orderBook = OrderBookFactory.importOrderBook(filePath)
//    val simulator = new DiscreteEventSimulator(
//      LocalDateTime.now(),
//      LocalDateTime.now().plusNanos(10000000),
//      traders,
//      List(orderBook))

    val simulator = new TimeSliceSimulator(LocalDateTime.now(),
      Duration.fromNanos(1e6),
      10,
      traders,
      List(orderBook))

    simulator.run()

    println(println(orderBook.transactionLog.toString))

  }

  def oldMain(args: Array[String]): Unit = {
    println("Hello world!")

    val startTime = LocalDateTime.now()

    val bestBuyPrice = 99
    val bestSellPrice = 101
    val standardOrderSize = 1
    val basicBuyOrder = Order(OrderType.Buy, bestBuyPrice, standardOrderSize)
    val basicSellOrder = Order(OrderType.Sell, bestSellPrice, standardOrderSize)

    val traders = TraderFactory.getBasicTraders()

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val transactionLog = new TransactionLog()
//    val orderBook = new OrderBook(askSide, bidSide, List(), transactionLog)
//    val orderBook = OrderBookFactory.getPopulatedOrderBook(20)

    val orderBook = OrderBookFactory.importOrderBook(filePath)
//    orderBook.submitOrder(sellTrader, basicSellOrder)
    //    orderBook.submitOrder(buyTrader, basicBuyOrder)

    val simulator = new TimeSliceSimulator(LocalDateTime.now(),
                                           Duration.fromNanos(1e6),
                                           1,
                                           traders,
                                           List(orderBook))

    simulator.run()
    println(orderBook.transactionLog.toString)

    println("Simulation finished")

  }

//  def startSimulation(): Unit = {
//    val trader = BestPriceRateTrader
//  }

}
