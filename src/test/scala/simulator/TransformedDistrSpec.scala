package simulator

import breeze.stats.distributions._
import org.scalatest.FlatSpec

import scala.Numeric.Implicits._

class TransformedDistrSpec extends FlatSpec {

  def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size

  def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)

    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(variance(xs))

  "sample" should "shift normal(0,1) by appropriate loc" in {
    val norm = new Gaussian(0, 1)
    val dist = new TransformedDistr(norm, loc = 10, scale = 1)

    val numValues = 1000000
    val sampledValues = Range(0, numValues).map(_ => dist.sample())

    val x = mean(sampledValues)
    val s = stdDev(sampledValues)

    assert(math.abs(x - 10) < 0.01)
    assert(math.abs(s - 1) < 0.01)
  }

  "sample" should "spread normal(0,1) by appropriate scale" in {
    val norm = new Gaussian(0, 1)
    val dist = new TransformedDistr(norm, loc = 0, scale = 10)

    val numValues = 1000000
    val sampledValues = Range(0, numValues).map(_ => dist.sample())

    println(sampledValues)

    val x = mean(sampledValues)
    val s = stdDev(sampledValues)

    assert(math.abs(x - 0) < 0.01)
    assert(math.abs(s - 10) < 0.01)
  }

  "sample" should "spread and shift normal(0,1) by appropriate loc and scale" in {
    val norm = new Gaussian(0, 1)
    val dist = new TransformedDistr(norm, loc = 20, scale = 10)

    val numValues = 1000000
    val sampledValues = Range(0, numValues).map(_ => dist.sample())

    println(sampledValues)

    val x = mean(sampledValues)
    val s = stdDev(sampledValues)

    assert(math.abs(x - 20) < 0.01)
    assert(math.abs(s - 10) < 0.01)
  }

}
