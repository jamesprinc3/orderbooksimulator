package simulator.sampling

import java.util.NoSuchElementException

import breeze.stats.distributions.{ContinuousDistr, Uniform}

import scala.math.BigDecimal.RoundingMode

/**
  * PRE: Assume each entry in pairs is of form (boundary, sample)
  */
class InverseCdfSampler(pairs: Seq[(BigDecimal, Double)], bucketWidth: BigDecimal) extends ContinuousDistr[Double] {

  val hashMap: Map[BigDecimal, Double] = generateHashMap(pairs, bucketWidth)

  /** Pre-process the cdf so that we can do O(1) lookup
    *
    */
  private def generateHashMap(pairs: Seq[(BigDecimal, Double)], bucketWidth: BigDecimal): Map[BigDecimal, Double] = {
    // We have to add dumb values on the end so that the indexing works of the cuff, this should really be engineered to not be such awful code. Alas, time is of the essence
    val sortedPairs = (Seq((BigDecimal.decimal(0).setScale(2), 0.0)) ++ pairs ++ Seq((BigDecimal.decimal(1).setScale(2), 0.0))).sortBy(p => p._1)
    var sampleMap = Map[BigDecimal, Double]()

    var index = 0
    while (index < sortedPairs.length - 1) {
      var key = sortedPairs(index)._1.setScale(2, RoundingMode.DOWN)
      val sample = sortedPairs(index)._2

      while (key <= sortedPairs(index + 1)._1) {
        sampleMap += (key -> sample)

        key += bucketWidth
      }
      index += 1
    }
    sampleMap
  }

  override def sample(): Double = {
    val uniformSample = Uniform(0, 1).sample()

    //

    try {
      hashMap(BigDecimal.decimal(uniformSample).setScale(2, RoundingMode.HALF_UP))
    } catch {
      case e: NoSuchElementException =>
        println(hashMap.keys.toSeq.sorted)
        throw e
    }

  }

  // TODO: maybe these could be given some more meaningful values?
  override def unnormalizedLogPdf(x: Double): Double = 0

  override def logNormalizer: Double = 0

  override def draw(): Double = 0
}
