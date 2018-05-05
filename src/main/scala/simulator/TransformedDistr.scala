package simulator

import breeze.stats.distributions.ContinuousDistr

class TransformedDistr(continuousDistr: ContinuousDistr[Double],
                               loc: Double,
                               scale: Double) {

  def sample(): Double = {
    (scale * continuousDistr.sample()) + loc
  }
}
