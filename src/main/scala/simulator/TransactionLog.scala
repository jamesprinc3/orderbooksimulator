package simulator

import java.io.File
import java.nio.file.{Files, Paths}

import com.github.tototoshi.csv.CSVWriter
import simulator.events.{Cancel, Trade}
import simulator.order.Order

import scala.collection.mutable.ListBuffer

class TransactionLog() {

  var trades: ListBuffer[Trade] = ListBuffer()
  var orders: ListBuffer[Order] = ListBuffer()
  var cancels: ListBuffer[Cancel] = ListBuffer()

  def addTrade(trade: Trade): Unit = {
    trades += trade
  }

  def addOrder(order: Order): Unit = {
    orders += order
  }

  def addCancel(cancel: Cancel): Unit = {
    cancels += cancel
  }

  def export(fileDir: String): Unit = {
    ensureDirectoryExists(fileDir)

    val orderHeader =
      List("time", "side", "trader_id", "order_id", "price", "size")
    val orderData: Seq[Seq[String]] = orders.map(order => order.toFields)
    writeEvents(fileDir + "orders.csv", orderHeader, orderData)

    val tradeHeader = List("time",
                           "buyer_id",
                           "buyer_order_id",
                           "seller_id",
                           "seller_order_id",
                           "price",
                           "size")
    val tradeData = trades.map(
      trade =>
        List(
          trade.time.toString,
          trade.buyerId.toString,
          trade.buyerOrderId.toString,
          trade.sellerId.toString,
          trade.sellerOrderId.toString,
          trade.price.toString,
          trade.size.toString
      ))
    writeEvents(fileDir + "trades.csv", tradeHeader, tradeData)

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
    res += trades.map(_.toString).mkString("\n") + "\n\n"

    res += "Cancels made:\n"
    res += cancels.map(_.toString).mkString("\n")

    res
  }
}
