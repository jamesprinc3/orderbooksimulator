package simulator.trader

import java.time.LocalDateTime

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import simulator.TransactionLog
import simulator.order.{Order, OrderType, Trade}
import simulator.orderbook.{OrderBook, OrderBookEntry}

// TODO: simulator.trader factory?
abstract class Trader(traderParams: TraderParams) {

  val id: Int = traderParams.id
  private var balance = traderParams.initialBalance
  private var holdings = traderParams.initialHoldings
  protected var virtualTime: LocalDateTime = LocalDateTime.now()
  protected var openOrders: Set[OrderBookEntry] = Set[OrderBookEntry]()
  protected var transactionLog = new TransactionLog

  private val logger = Logger(this.getClass)

  def updateState(trade: Trade): Unit = {
//    logger.debug("UPDATE TRADE - trader " + this.id.toString + " " + openOrders.toString())
    transactionLog.addTrade(trade)
    logger.debug(trade.toString)
    val diff = trade.price * trade.size
    id match {
      case trade.buyerId =>
        balance -= diff
        holdings += trade.size
        removeOpenOrder(trade.buyerOrderId, trade)

      case trade.sellerId =>
        balance += diff
        holdings -= trade.size
        removeOpenOrder(trade.sellerOrderId, trade)
    }
//    logger.debug("UPDATE TRADE - trader " + this.id.toString + " " + openOrders.toString())
  }

  private def removeOpenOrder(tradedOrderId: Int, trade: Trade): Unit = {
    openOrders.find(_.orderId == tradedOrderId) match {
      case Some(order) =>
        openOrders -= order
        if (order.size != trade.size) {
          openOrders += order.copy(size = order.size - trade.size)
        }
      case None =>
        logger.debug("Illegal State!")
        logger.debug("trader " + this.id.toString + " " + openOrders.toString())
        logger.debug(trade.toString)
        logger.debug(tradedOrderId.toString)
        logger.debug(this.id.toString)
        throw new IllegalStateException()
    }
  }

  // TODO: maybe we can just calculate these values by looping through the openOrders (but performance might be poor)
  def updateState(order: OrderBookEntry): Unit = {
    transactionLog.addOrder(order)
//    logger.debug("UPDATE ORDER - trader " + this.id.toString + " " + openOrders.toString())
    openOrders += order

    val diff = order.price * order.size
    order.orderType match {
      case OrderType.Buy =>
        balance -= diff
      case OrderType.Sell =>
        holdings -= order.size
    }
//    logger.debug("UPDATE ORDER - trader " + this.id.toString + " " + openOrders.toString())
  }

  def cancelOrder(order: OrderBookEntry): Unit = {
//    logger.debug("CANCEL ORDER - trader " + this.id.toString + " " + openOrders.toString())
    openOrders -= order

    val diff = order.price * order.size
    order.orderType match {
      case OrderType.Buy =>
        balance += diff
      case OrderType.Sell =>
        holdings += order.size
    }
//    logger.debug("CANCEL ORDER - trader " + this.id.toString + " " + openOrders.toString())
  }

  def getBalance: Double = {
    balance
  }

  def getHoldings: Double = {
    holdings
  }

  def getOpenOrders: Set[OrderBookEntry] = {
    openOrders
  }

  def getTransactionLog: TransactionLog = {
    transactionLog
  }

  def initialStep(orderBooks: List[OrderBook] = List())
  : List[(LocalDateTime, Trader, OrderBook, Order)]

  def step(newTime: LocalDateTime, orderBooks: List[OrderBook] = List())
    : List[(LocalDateTime, Trader, OrderBook, Order)]
}
