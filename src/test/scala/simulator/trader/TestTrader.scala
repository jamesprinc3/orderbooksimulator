package simulator.trader
import java.time.LocalDateTime

class TestTrader(traderParams: TraderParams)
  extends Trader(traderParams) {

  override def step(newTime: LocalDateTime): Unit = {}

}
