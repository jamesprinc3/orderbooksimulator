package simulator.logs

import simulator.traits.Loggable

import scala.collection.mutable.ListBuffer

class Log[T <: Loggable] {

  private val logBuffer: ListBuffer[T] = ListBuffer[T]()

  def add(newElem: T): Unit = {
    logBuffer += newElem
  }

  // Turn the log into a series of strings which could then be written to a CSV file
  def toCsvString: Seq[Seq[String]] = {
    logBuffer.map(elem => elem.toCsvString)
  }

}
