package simulator.simulators

trait Simulator {

  private val logger = com.typesafe.scalalogging.Logger(this.getClass)
  var success = true

  def endCondition(): Boolean

  def updateState(): Boolean

  def initialState(): Unit

  def run(): Unit = {
    val t0 = System.nanoTime()

    initialState()
    while (!endCondition()) {
      if (!updateState()) {
        success = false
      }
    }
    val t1 = System.nanoTime()

    logger.info(s"Simulation took ${(t1 - t0) / 1e9} seconds")

  }
}
