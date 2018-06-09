package simulator.sampling

import breeze.linalg._
import breeze.stats._
import org.scalatest.FlatSpec

class InverseCdfSamplerSpec extends FlatSpec {


  def corr(a: DenseVector[Double], b: DenseVector[Double]): Double = {
    if (a.length != b.length)
      sys.error("you fucked up")

    val n = a.length

    val astddev = math.sqrt(variance(a))
    val bstddev = math.sqrt(variance(b))

    1.0 / (n - 1.0) * sum(((a - mean(a)) / astddev) :* ((b - mean(b)) / bstddev))
  }

  private val decimalPlaces = 2
  private val bucketWidth = BigDecimal.decimal(0.01)
  private val pairs = Seq((BigDecimal.decimal(0), 0.5),
    (BigDecimal.decimal(0.1), 1.0),
    (BigDecimal.decimal(0.2), 2.0))
  private val smallInverseCdfSampler = new InverseCdfSampler(pairs, decimalPlaces)
  private val sortedKeys = smallInverseCdfSampler.hashMap.keys.toSeq.sorted

  "hashMap" should "produce equally spaced keys" in {
    assert(
      (sortedKeys drop 1, sortedKeys).zipped
        .map(_ - _)
        .forall(v => v == bucketWidth))
  }

  it should "produce correct number of keys" in {
    assert(sortedKeys.length == BigDecimal.decimal(1) / bucketWidth)
  }

  val norm = new breeze.stats.distributions.Gaussian(0, 1)
  val numSamples = 10000
  val xs = norm.sample(numSamples).map(x => math.abs(x)).sorted

  // Normalise ys
  val ys = xs.map(v => v / xs.sum)
  val cumulatedYs = ys.map {
    var s = 0.0; d => {
      s += d; s
    }
  }.map(y => BigDecimal.decimal(y))

  val normPairs = cumulatedYs zip xs
  val normInverseCdfSampler = new InverseCdfSampler(normPairs, decimalPlaces)

  it should "produce equally spaced keys for normal distribution" in {
    val normKeys = normInverseCdfSampler.hashMap.keys.toSeq.sorted

    assert(
      (normKeys drop 1, sortedKeys).zipped
        .map(_ - _)
        .forall(v => v == bucketWidth))
  }

  "sample" should "produce norm distribution" in {
    //    val bucketSize = BigDecimal.decimal(0.01)

    val inverseSamples = Range(0, numSamples).map(_ => normInverseCdfSampler.sample()).sorted

    print(corr(DenseVector(inverseSamples.toArray), DenseVector(xs.toArray)))
  }


}
