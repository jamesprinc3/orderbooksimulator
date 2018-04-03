package simulator.order

import java.time.LocalDateTime

// TODO: maybe add an exchange/orderbook id here too, if we want to have a global log of transactions
case class Trade(time: LocalDateTime, buyerId: Int, buyedOrderId: Int, sellerId: Int, sellerOrderId: Int, price: Int, size: Int)
