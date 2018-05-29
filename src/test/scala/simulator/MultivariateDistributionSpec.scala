package simulator

import breeze.linalg._
import breeze.stats.distributions.Gaussian
import org.scalatest.FlatSpec

import scala.Numeric.Implicits._

class MultivariateDistributionSpec extends FlatSpec {

  private val mu = 0
  private val sigma = 1

  private val d1 = new TransformedDistr(new Gaussian(mu, sigma), mu, sigma)
  private val d2 = new TransformedDistr(new Gaussian(mu, sigma), mu, sigma)
  private val covMatrix = DenseMatrix((1.0, 0.5), (0.5, 1.0))

  private val _preload = 10
  private val _stress = 100000
  private val _eps = 0.01

  private def _smallMultivariateDistribution = new MultivariateDistribution(d1, d2, _preload, covMatrix)

  private def _largeMultivariateDistribution = new MultivariateDistribution(d1, d2, _stress, covMatrix)

  private def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size

  private def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)

    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  private def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(variance(xs))

  "init" should "load with correct number of preloaded values" in {
    val multivariateDistribution = new MultivariateDistribution(d1, d2, _preload)

    assert(multivariateDistribution.remaining() == 10)
  }

  "sample" should "produce sample with correct mean" in {
    val distr = _largeMultivariateDistribution
    val samples = Range(0, _stress).map(_ => distr.sample())

    val d1Samples = samples.map(tup => tup._1)
    val d2Samples = samples.map(tup => tup._2)

    assert(math.abs(d1Samples.sum / d1Samples.length) < _eps)
    assert(math.abs(d2Samples.sum / d2Samples.length) < _eps)
  }

  it should "produce sample with correct sigma" in {
    val distr = _largeMultivariateDistribution
    val samples = Range(0, _stress).map(_ => distr.sample())

    val d1Samples = samples.map(tup => tup._1)
    val d2Samples = samples.map(tup => tup._2)

    assert(math.abs(stdDev(d1Samples) - sigma) < _eps)
    assert(math.abs(stdDev(d2Samples) - sigma) < _eps)
  }

  it should "increase number of samples when exceeding preloaded" in {
    val distr = _smallMultivariateDistribution
    val samples = Range(0, _preload + 1).map(_ => distr.sample())

    assert(samples.length == _preload + 1)
  }


  it should "have correct number of remaining samples after exceeding preloaded samples" in {
    val distr = _smallMultivariateDistribution
    Range(0, _preload + 1).foreach(_ => distr.sample())

    assert(distr.remaining() == _preload - 1)
  }

  it should "produce values with correct covariance" in {
    val numSamples = 10000

    val distr = new MultivariateDistribution(d1, d2, numSamples, covMatrix)
    val samples = Range(0, numSamples + 1).map(_ => distr.sample())

    val unzipped = samples.unzip
    val samplesMatrix = DenseMatrix(unzipped._1, unzipped._2).t

    val sampleCovMat = cov(samplesMatrix)
    val residual = sampleCovMat - covMatrix

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
  }

  "getCorrelatedSamples" should "produce values with correct covariance" in {
    val numSamples = 10000

    val uncorrelatedSamples =
      DenseMatrix(d1.sample(numSamples), d2.sample(numSamples)).reshape(numSamples, 2)

    println(cov(uncorrelatedSamples))

    val distr = new MultivariateDistribution(d1, d2, numSamples, covMatrix)
    val samples = distr.getCorrelatedSamples(d1, d2, covMatrix)

    val sampleCovMat = cov(samples)
    val residual = sampleCovMat - covMatrix

    println(sampleCovMat)
    println(covMatrix)

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
  }

}
