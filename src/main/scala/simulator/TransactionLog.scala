package simulator

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.github.tototoshi.csv.CSVWriter
import simulator.order.{Cancel, Trade}
import simulator.orderbook.OrderBookEntry

class TransactionLog() {

  var trades: List[Trade] = List()
  var orders: List[OrderBookEntry] = List()
  var cancels: List[Cancel] = List()

  def addTrade(trade: Trade): Unit = {
    trades = trades ::: List(trade)
  }

  def addOrder(order: OrderBookEntry): Unit = {
    orders = orders ::: List(order)
  }

  def addCancel(cancel: Cancel): Unit = {
    cancels = cancels ::: List(cancel)
  }

  def export(fileDir: String): Unit = {
    ensureDirectoryExists(fileDir)

    val orderHeader =
      List("time", "side", "trader_id", "order_id", "price", "size")
    val orderData: Seq[Seq[String]] = orders.map(
      order =>
        Seq(order.arrivalTime.toString,
            order.orderType.toString.toLowerCase(),
            order.trader.id.toString,
            order.orderId.toString,
            order.price.toString,
            order.size.toString))
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
      "arrivalTime", "side", "trader_id", "order_id", "price", "size")
    val cancelData: Seq[Seq[String]] = cancels.map(
      cancel => {
        Seq(cancel.time.toString,
          cancel.order.arrivalTime.toString,
          cancel.order.orderType.toString.toLowerCase,
          cancel.order.trader.id.toString,
          cancel.order.orderId.toString,
          cancel.order.price.toString,
          cancel.order.size.toString)
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
