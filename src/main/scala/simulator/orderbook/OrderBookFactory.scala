package simulator.orderbook

import java.io.File
import java.time.LocalDateTime

import breeze.stats.distributions._
import com.github.tototoshi.csv._
import simulator.Side
import simulator.order.{LimitOrder, Order}
import simulator.orderbook.priority.PriceSize
import simulator.trader.TraderFactory

object OrderBookFactory {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  def getOrderBook(orders: List[Order] = List()): OrderBook = {

    val askSide = new OrderBookSide(Side.Ask, new PriceSize(Side.Ask))
    val bidSide = new OrderBookSide(Side.Bid, new PriceSize(Side.Bid))

    val orderBook = new OrderBook(askSide, bidSide, orders)

    orderBook
  }

  /**
    * Returns an order book which has been populated with orders picked from a distribution
    */
//  def getPopulatedOrderBook(n: Int): OrderBook = {
//    val buySidePrice = new Gaussian(10000, 1000)
//    val sellSidePrice = new Gaussian(6000, 1000)
//
//    val buyOrders = Range(0, n).map(x => {
//      Order(Side.Bid, buySidePrice.sample(), 1)
//    }).toList
//
//    val sellOrders = Range(0, n).map(x => {
//      Order(Side.Ask, sellSidePrice.sample(), 1)
//    }).toList
//
//    getOrderBook(buyOrders ++ sellOrders)
//  }

  /**
    *
    * @param filePath path to a CSV file
    * @return an OrderBook populated with the orders contained within the CSV file
    */
  def importOrderBook(filePath: String, startTime: LocalDateTime): OrderBook = {
    val reader = CSVReader.open(new File(filePath))
    val handsOffTrader = TraderFactory.getHandsOffTrader
    val orders = reader.allWithHeaders().map(order => {
      val side = order("side") match {
        case "buy" => Side.Bid
        case "sell" => Side.Ask
      }

      LimitOrder(startTime, side, handsOffTrader, order("price").toDouble, order("size").toDouble)
    })

    logger.debug(orders.sortBy(order => order.price).mkString("\n"))

    getOrderBook(orders)
  }

}
