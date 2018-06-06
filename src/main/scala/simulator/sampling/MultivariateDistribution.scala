package simulator.sampling

import breeze.linalg.{DenseMatrix, cholesky}

class MultivariateDistribution(d1: TransformedDistr,
                               d2: TransformedDistr,
                               preload: Int = 500,
                               val covMatrix: DenseMatrix[Double] =
                               DenseMatrix((1.0, 0.0), (0.0, 1.0))) {

  private var correlatedSamples = getCorrelatedSamples(d1, d2, covMatrix)
  private var index = 0

  def getCorrelatedSamples(
                            d1: TransformedDistr,
                            d2: TransformedDistr,
                            covMatrix: DenseMatrix[Double]): DenseMatrix[Double] = {
    val L = cholesky(covMatrix)
    val uncorrelatedSamples =
      DenseMatrix(d1.sample(preload), d2.sample(preload))

    val correlatedSamples = L * uncorrelatedSamples

    correlatedSamples.t
  }

  def sample(): (Double, Double) = {
    if (index >= correlatedSamples.rows) {
      correlatedSamples = getCorrelatedSamples(d1, d2, covMatrix)
      index = 0
    }

    val ret = (correlatedSamples(index, 0), correlatedSamples(index, 1))
    index += 1

    ret
  }

  def remaining(): Int = {
    correlatedSamples.rows - index
  }

}
