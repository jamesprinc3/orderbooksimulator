package simulator

import java.time.LocalDateTime

abstract class Steppable(startTime: LocalDateTime) {

  protected var virtualTime: LocalDateTime = startTime

  def step(newTime: LocalDateTime): Unit = {
    virtualTime = newTime
  }

}
