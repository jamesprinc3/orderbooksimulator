package simulator.orderbook

import java.io.File

import simulator.order.{Order, OrderType}
import breeze.stats.distributions._
import com.github.tototoshi.csv._
import simulator.Main.logger

object OrderBookFactory {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)

  def getOrderBook(orders: List[Order] = List()): OrderBook = {

    val askSide = new OrderBookSide(OrderBookSideType.Ask)
    val bidSide = new OrderBookSide(OrderBookSideType.Bid)

    val orderBook = new OrderBook(askSide, bidSide, orders)

    orderBook
  }

  /**
    * Returns an order book which has been populated with orders picked from a distribution
    */
  def getPopulatedOrderBook(n: Int): OrderBook = {
    val buySidePrice = new Gaussian(10000, 1000)
    val sellSidePrice = new Gaussian(6000, 1000)

    val buyOrders = Range(0, n).map(x => {
      Order(OrderType.Buy, buySidePrice.sample(), 1)
    }).toList

    val sellOrders = Range(0, n).map(x => {
      Order(OrderType.Sell, sellSidePrice.sample(), 1)
    }).toList

    getOrderBook(buyOrders ++ sellOrders)
  }

  /**
    *
    * @param filePath path to a CSV file
    * @return an OrderBook populated with the orders contained within the CSV file
    */
  def importOrderBook(filePath: String): OrderBook = {
    val reader = CSVReader.open(new File(filePath))
    val orders = reader.allWithHeaders().map(order => {
      val orderType = order("side") match {
        case "buy" => OrderType.Buy
        case "sell" => OrderType.Sell
      }
      Order(orderType, order("price").toDouble, order("size").toDouble)
    })

    logger.debug(orders.sortBy(order => order.price).mkString("\n"))

    getOrderBook(orders)
  }

}
