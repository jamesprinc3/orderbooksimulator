package simulator.order

object OrderType extends Enumeration {
  val Buy, Sell = Value
}

// TODO: just parameterise this with a Buy/Sell type?
case class Order(orderType: OrderType.Value, price: Int, size: Int) {

}

