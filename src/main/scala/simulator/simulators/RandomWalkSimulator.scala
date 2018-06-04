package simulator.simulators

class RandomWalkSimulator(startPrice: Double, maxSteps: Int) extends Simulator {

  private var steps = 0
  private var price = startPrice

  override def endCondition(): Boolean = {
    steps >= maxSteps
  }

  override def updateState(): Unit = {


    steps += 1
  }

  override def initialState(): Unit = {

  }
}
