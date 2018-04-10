package simulator.simulators

abstract class Simulator {

  def endCondition(): Boolean

  def updateState(): Unit

  def run(): Unit = {
    while(!endCondition()) {
      updateState()
    }
  }

}
