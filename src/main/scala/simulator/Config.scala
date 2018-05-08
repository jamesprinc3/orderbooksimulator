package simulator

import breeze.stats.distributions._
import com.typesafe.scalalogging.Logger
import spray.json._

import scala.io.Source

case class Config(numSimulations: Int = 1,
                  parallel: Boolean = false,
                  simRoot: String = "",
                  orderBookPath: String = "",
                  buyOrderRatio: Double = 0.5,
                  buyVolumeRatio: Double = 0.5,
                  distributions: Map[String, TransformedDistr] =
                    Map[String, TransformedDistr]())

object Config {
  def init(numSimulations: Int,
           parallel: Boolean,
           simRootPath: String,
           paramsPath: String,
           orderBookPath: String): Config = {
    val distributions = parseDistributions(paramsPath)
    val buyOrderRatio = getBuyOrderRatio(paramsPath)
    val buyVolumeRatio = getBuyVolumeRatio(paramsPath)

    Config(numSimulations,
           parallel,
           simRootPath,
           orderBookPath,
           buyOrderRatio,
           buyVolumeRatio,
           distributions)
  }

  object DistJsonProtocol extends DefaultJsonProtocol {

    private val logger = Logger(this.getClass)

    implicit object DistJsonFormat extends RootJsonFormat[TransformedDistr] {
      def write(dist: TransformedDistr): JsString = {
        JsString("Not implemented")
      }

      def read(value: JsValue): TransformedDistr = value match {
        case JsString(distString) =>
          logger.debug(distString)
          val distSplit = distString.split('(')
          val coeffsStr = distSplit(1).stripSuffix(")").split(", ")
          val coeffs: Map[String, Double] = coeffsStr
            .map(s => (s.split('=')(0), s.split('=')(1).toDouble))
            .toMap

          var loc = coeffs("loc")
          var scale = coeffs("scale")

          val contDist = distSplit(0) match {
            case "expon" =>
              val ret = new Exponential(1 / scale)
              scale = 1
              ret
            case "lognorm" =>
              val stdDev = coeffs("s")
              val ret = new LogNormal(Math.log(scale), stdDev)
              scale = 1
              ret
            case "cauchy" =>
              val ret = new CauchyDistribution(loc, scale)
              loc = 0
              scale = 1
              ret
            case "f" =>
              val numeratorDegFreedom = coeffs("dfn")
              val denominatorDegFreedom = coeffs("dfd")
              new FDistribution(numeratorDegFreedom, denominatorDegFreedom)
            case "beta" =>
              val a = coeffs("a")
              val b = coeffs("b")
              new Beta(a, b)
            case "gamma" =>
              val a = coeffs("a")
              val ret = new Gamma(a, scale)
              scale = 1
              ret
            case "chi2" =>
              val df = coeffs("df")
              new ChiSquared(df)
          }

          new TransformedDistr(contDist, loc, scale)
      }
    }

  }

  def getBuyOrderRatio(parametersPath: String): Double = {
    val json = Source.fromFile(parametersPath).getLines.mkString("")
    val jsonAst = json.parseJson

    jsonAst.asJsObject().fields("buy_sell_order_ratio") match {
      case JsArray(Vector(JsNumber(bRatio), JsNumber(sRatio))) =>
        bRatio.toDouble
    }
  }

  def getBuyVolumeRatio(parametersPath: String): Double = {
    val json = Source.fromFile(parametersPath).getLines.mkString("")
    val jsonAst = json.parseJson

    jsonAst.asJsObject().fields("buy_sell_volume_ratio") match {
      case JsArray(Vector(JsNumber(bRatio), JsNumber(sRatio))) =>
        bRatio.toDouble
    }
  }

  def parseDistributions(
      parametersPath: String): Map[String, TransformedDistr] = {

    val json = Source.fromFile(parametersPath).getLines.mkString("")
    val jsonAst = json.parseJson

    import DistJsonProtocol._
    val distributions = jsonAst.asJsObject
      .fields("distributions")
      .asJsObject
      .fields
      .map(kv => (kv._1, kv._2.convertTo[TransformedDistr]))
    println(distributions)
    //.convertTo[Map[String, TransformedDistr]]

    distributions
  }
}
