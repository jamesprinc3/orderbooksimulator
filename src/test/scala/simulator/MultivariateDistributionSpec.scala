package simulator

import breeze.linalg._
import breeze.stats._
import breeze.stats.distributions._
import org.scalatest.FlatSpec

import scala.math.sqrt

class MultivariateDistributionSpec extends FlatSpec {

  private val mu = 0
  private val sigma = 1

  private val n1 = new TransformedDistr(new Gaussian(mu, sigma), mu, sigma)
  private val n2 = new TransformedDistr(new Gaussian(mu, sigma), mu, sigma)

  private val c1 = new TransformedDistr(new CauchyDistribution(mu, sigma), mu, sigma)
  private val c2 = new TransformedDistr(new CauchyDistribution(mu, sigma), mu, sigma)
  private val covMatrix = DenseMatrix((1.0, 0.5), (0.5, 1.0))

  private val _preload = 10
  private val _stress = 100000
  private val _eps = 0.01

  private def _smallMultivariateDistribution = new MultivariateDistribution(n1, n2, _preload, covMatrix)

  private def _largeMultivariateDistribution = new MultivariateDistribution(n1, n2, _stress, covMatrix)

  //  private def stdDev[Double](xs: Iterable[Double]): Double = math.sqrt(variance(xs))

  /**
    * Inspired by https://gist.github.com/tbertelsen/4353d4a8a4386afb0abb
    */
  private def pearson(a: Vector[Double], b: Vector[Double]): Double = {
    if (a.length != b.length)
      throw new IllegalArgumentException("Vectors not of the same length.")

    val n = a.length

    val dot = a.dot(b)
    val adot = a.dot(a)
    val bdot = b.dot(b)
    val amean = mean(a)
    val bmean = mean(b)

    // See Wikipedia http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient#For_a_sample
    (dot - n * amean * bmean) / (sqrt(adot - n * amean * amean) * sqrt(bdot - n * bmean * bmean))
  }

  "init" should "load with correct number of preloaded values" in {
    val multivariateDistribution = new MultivariateDistribution(n1, n2, _preload)

    assert(multivariateDistribution.remaining() == 10)
  }

  private def samplesHaveCorrectCovMatrix(d1: TransformedDistr, d2: TransformedDistr, covMatrix: DenseMatrix[Double]) = {
    val numSamples = 10000

    val d1Samples = d1.sample(numSamples)
    val d2Samples = d2.sample(numSamples)

    val uncorrelatedSamples =
      DenseMatrix(d1Samples, d2Samples)

    println(pearson(DenseVector(d1Samples.toArray), DenseVector(d2Samples.toArray)))
    println(pearson(uncorrelatedSamples(0, ::).inner, uncorrelatedSamples(1, ::).inner))

    println(uncorrelatedSamples(0, ::).inner)
    println(uncorrelatedSamples(1, ::).inner)

    val distr = new MultivariateDistribution(d1, d2, numSamples, covMatrix)
    val samples = distr.getCorrelatedSamples(d1, d2, covMatrix)

    println(samples)

    //    val sampleCorr: Double = pearson(DenseVector(samples(::,0).data), DenseVector(samples(::, 1).data))
    ////    val sampleCovMat = DenseMatrix((1.0, sampleCorr), (sampleCorr, 1.0))
    val sampleCovMat = cov(uncorrelatedSamples.t)
    val residual = sampleCovMat - covMatrix

    println(sampleCovMat)
    println(covMatrix)

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
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

    assert(math.abs(stddev(d1Samples) - sigma) < _eps)
    assert(math.abs(stddev(d2Samples) - sigma) < _eps)
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

    val distr = new MultivariateDistribution(n1, n2, numSamples, covMatrix)
    val samples = Range(0, numSamples + 1).map(_ => distr.sample())

    val unzipped = samples.unzip
    val samplesMatrix = DenseMatrix(unzipped._1, unzipped._2).t

    val sampleCovMat = cov(samplesMatrix)
    val residual = sampleCovMat - covMatrix

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
  }

  it should "produce values with correct covariance when after exceeding preloaded samples for normal distribution" in {
    val numSamples = 10000

    val distr = new MultivariateDistribution(n1, n2, numSamples, covMatrix)
    val samples1 = Range(0, numSamples + 1).map(_ => distr.sample())
    val samples2 = Range(0, numSamples + 1).map(_ => distr.sample())

    val samples = samples1 ++ samples2

    val unzipped = samples.unzip
    val samplesMatrix = DenseMatrix(unzipped._1, unzipped._2).t

    val sampleCovMat = cov(samplesMatrix)
    val residual = sampleCovMat - covMatrix

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
  }

  "getCorrelatedSamples" should "produce values with correct covariance for normal distribution" in {
    val numSamples = 10000

    val uncorrelatedSamples =
      DenseMatrix(n1.sample(numSamples), n2.sample(numSamples)).reshape(numSamples, 2)

    println(cov(uncorrelatedSamples))

    val distr = new MultivariateDistribution(n1, n2, numSamples, covMatrix)
    val samples = distr.getCorrelatedSamples(n1, n2, covMatrix)

    val sampleCovMat = cov(samples)
    val residual = sampleCovMat - covMatrix

    println(sampleCovMat)
    println(covMatrix)

    assert(residual.toArray.map(r => math.abs(r)).sum < 0.1)
  }

  "getCorrelatedSamples" should "produce values with correct covariance for one normal, one cauchy" in {
    samplesHaveCorrectCovMatrix(n1, c1, covMatrix)
  }

  // TODO: ensure that values produced still fit the original distributions (try with new distributions)

}
