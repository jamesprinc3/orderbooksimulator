package simulator.sampling

import breeze.stats.distributions.ContinuousDistr

class TransformedDistr(continuousDistr: ContinuousDistr[Double],
                               loc: Double,
                               scale: Double) {

  def sample(): Double = {
    (scale * continuousDistr.sample()) + loc
  }

  // TODO: Consider acceleration with .par
  def sample(n: Int): Seq[Double] = {
    Range(0, n).map(_ => sample())
  }
}
