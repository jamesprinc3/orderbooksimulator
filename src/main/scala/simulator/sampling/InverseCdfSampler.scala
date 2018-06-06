package simulator.sampling

import breeze.stats.distributions.Uniform

import scala.math.BigDecimal.RoundingMode

/**
  * PRE: Assume each entry in pairs is of form (boundary, sample)
  */
class InverseCdfSampler(pairs: Seq[(BigDecimal, Double)], bucketWidth: BigDecimal) {

  val hashMap: Map[BigDecimal, Double] = generateHashMap(pairs, bucketWidth)

  /** Pre-process the cdf so that we can do O(1) lookup
    *
    */
  private def generateHashMap(pairs: Seq[(BigDecimal, Double)], bucketWidth: BigDecimal): Map[BigDecimal, Double] = {
    // We have to add a dumb value on the end so that the indexing works of the cuff, this should really be engineered to not be such awful code. Alas, time is of the essence
    val sortedPairs = (pairs ++ Seq((BigDecimal.decimal(1).setScale(2), 0.0))).sortBy(p => p._1)
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

  def sample(): Double = {
    val uniformSample = Uniform(0, 1).sample()

    hashMap(BigDecimal.decimal(uniformSample).setScale(2, RoundingMode.HALF_UP))
  }

}
