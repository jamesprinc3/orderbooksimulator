package simulator.logs

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

import com.github.tototoshi.csv.CSVWriter
import simulator.events.{Cancel, DoublePrice, OrderBookEntry, Trade}
import simulator.order.{LimitOrder, MarketOrder, Order}

import scala.collection.mutable.ListBuffer

class OrderBookLog() {

  //  var trades: ListBuffer[Trade] = ListBuffer()
  val trades: Log[Trade] = new Log[Trade]
  var orders: ListBuffer[Order] = ListBuffer()
  var cancels: ListBuffer[Cancel] = ListBuffer()
  var midPrices: ListBuffer[(LocalDateTime, Double)] = ListBuffer[(LocalDateTime, Double)]()
  var spreads: Log[DoublePrice] = new Log[DoublePrice]()
  var bestBids: Log[DoublePrice] = new Log[DoublePrice]()
  var bestAsks: Log[DoublePrice] = new Log[DoublePrice]()

  var finalOrderBookEntries: Log[OrderBookEntry] = new Log[OrderBookEntry]()

  def addTrade(trade: Trade): Unit = {
    trades.add(trade)
  }

  def addOrder(order: Order): Unit = {
    orders += order
  }

  def addCancel(cancel: Cancel): Unit = {
    cancels += cancel
  }

  def addMidprice(time: LocalDateTime, midPrice: Double) = {
    midPrices += (time -> midPrice)
  }

  def addSpread(time: LocalDateTime, spread: Double): Unit = {
    spreads.add(DoublePrice(time, spread))
  }

  def addBidPrice(time: LocalDateTime, bidPrice: Double): Unit = {
    bestBids.add(DoublePrice(time, bidPrice))
  }

  def addAskPrice(time: LocalDateTime, askPrice: Double): Unit = {
    bestAsks.add(DoublePrice(time, askPrice))
  }

  def addOrderBookEntry(orderBookEntry: OrderBookEntry) = {
    finalOrderBookEntries.add(orderBookEntry)
  }

  def export(fileDir: String): Unit = {
    ensureDirectoryExists(fileDir)

    // TODO: refactor these to use Log and LogWriter instead.
    writeOrderCsv(fileDir)
    writeTradeCsv(fileDir)
    writeCancelCsv(fileDir)
    writeMidpriceCsv(fileDir)
    LogWriter.writeEvents(fileDir + "spreads.csv", DoublePrice.getCsvHeader, spreads.toCsvString)
    LogWriter.writeEvents(fileDir + "bestBids.csv", DoublePrice.getCsvHeader, bestBids.toCsvString)
    LogWriter.writeEvents(fileDir + "bestAsks.csv", DoublePrice.getCsvHeader, bestAsks.toCsvString)
    LogWriter.writeEvents(fileDir + "finalOrderBook.csv", OrderBookEntry.getCsvHeader, finalOrderBookEntries.toCsvString)

  }

  private def writeOrderCsv(fileDir: String) = {
    val orderHeader =
      List("order_type", "time", "side", "trader_id", "price", "size")
    val orderData: Seq[Seq[String]] = orders.map {
      case order: LimitOrder => "limit" +: order.toFields
      case order: MarketOrder => "market" +: order.toFields
    }

    writeEvents(fileDir + "orders.csv", orderHeader, orderData)
  }

  private def writeTradeCsv(fileDir: String) = {
    val tradeHeader = Trade.getCsvHeader
    val tradeData = trades.toCsvString
    writeEvents(fileDir + "trades.csv", tradeHeader, tradeData)
  }

  private def writeCancelCsv(fileDir: String) = {
    val cancelHeader = List("time",
      "arrivalTime",
      "side",
      "trader_id",
      "order_id",
      "price",
      "size")
    val cancelData: Seq[Seq[String]] = cancels.map(
      cancel => {
        Seq(
          cancel.time.toString,
          cancel.order.time.toString,
          cancel.order.side.toString.toLowerCase,
          cancel.order.trader.id.toString,
          cancel.order.orderId.toString,
          cancel.order.price.toString,
          cancel.order.size.toString
        )
      }
    )
    writeEvents(fileDir + "cancels.csv", cancelHeader, cancelData)
  }

  private def writeMidpriceCsv(fileDir: String) = {
    val midPriceHeader =
      List("time", "price")
    val midPriceData: Seq[Seq[String]] = midPrices.map { kv => {
      Seq(kv._1.toString, kv._2.toString)
    }
    }.toSeq

    writeEvents(fileDir + "midprice.csv", midPriceHeader, midPriceData)
  }

  private def ensureDirectoryExists(dir: String): Unit = {
    if (!Files.exists(Paths.get(dir))) {
      new File(dir).mkdir()
    }
  }

  private def writeEvents(filePath: String,
                          header: Seq[String],
                          data: Seq[Seq[String]]): Unit = {

    val writer = CSVWriter.open(filePath)

    writer.writeRow(header)
    writer.writeAll(data)
  }

  override def toString: String = {
    var res = "Orders made:\n"
    res += orders.map(_.toString).mkString("\n") + "\n\n"

    res += "Trades made:\n"
    res += trades.toCsvString.map(line => line.mkString(",")).mkString("\n")

    res += "Cancels made:\n"
    res += cancels.map(_.toString).mkString("\n")

    res
  }
}
