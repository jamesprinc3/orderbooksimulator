package simulator.orderbook

import java.io.File
import java.time.LocalDateTime

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
    *
    * @param filePath path to a CSV file
    * @return an OrderBook populated with the orders contained within the CSV file
    */
  def importOrders(filePath: String, startTime: LocalDateTime): List[Order] = {
    val reader = CSVReader.open(new File(filePath))
    val handsOffTrader = TraderFactory.getHandsOffTrader
    var totalSize = 0.0
    var o: Map[String, String] = Map[String, String]()
    try {
      reader.allWithHeaders().map(order => {

        o = order
        val side = order("side") match {
          case "buy" => Side.Bid
          case "sell" => Side.Ask
        }

        totalSize += order("size").toDouble

        LimitOrder(startTime, side, handsOffTrader, order("price").toDouble, order("size").toDouble)

      })
    } catch {
      case e: Exception =>
        logger.debug(e.toString)
        List()
    }

  }

}
