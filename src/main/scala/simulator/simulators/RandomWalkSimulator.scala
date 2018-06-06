package simulator.simulators

import simulator.events.IntPrice
import simulator.logs.Log

import scala.util.Random


class RandomWalkSimulator(startPrice: Int, maxSteps: Int) extends Simulator {

  private var steps = 0
  private var price = startPrice
  val log = new Log[IntPrice]

  override def endCondition(): Boolean = {
    steps >= maxSteps
  }

  override def updateState(): Unit = {
    if (Random.nextDouble() > 0.5) {
      price += 1
    } else {
      price -= 1
    }

    log.add(new IntPrice(steps, price))

    steps += 1
  }

  override def initialState(): Unit = {

  }
}
