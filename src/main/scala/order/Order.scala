package order

object OrderType extends Enumeration {
  val Buy, Sell = Value
}

case class Order(orderType: OrderType.Value, price: Int, size: Int)

