package simulator

object Side extends Enumeration {
  type Side = Value
  val Bid: simulator.Side.Value = Value("buy")
  val Ask: simulator.Side.Value = Value("sell")
}
