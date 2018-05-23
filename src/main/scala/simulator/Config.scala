package simulator

import breeze.stats.distributions._
import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.Logger
import spray.json._

import scala.io.Source

case class Config(numSimulations: Int = 1,
                  simulationSeconds: Int = 300,
                  numTraders: Int = 1,
                  parallel: Boolean = false,
                  logLevel: Level,
                  simRoot: String = "",
                  orderBookPath: String = "",
                  ratios: Map[String, Double],
                  distributions: Map[String, TransformedDistr] =
                    Map[String, TransformedDistr]())

object Config {

  def init(numSimulations: Int,
           simulationSeconds: Int,
           numTraders: Int,
           parallel: Boolean,
           logLevelStr: String,
           simRootPath: String,
           paramsPath: String,
           orderBookPath: String): Config = {
    val logLevel = ch.qos.logback.classic.Level.toLevel(logLevelStr)

    val json = Source.fromFile(paramsPath).getLines.mkString("")
    val jsonAst = json.parseJson

    val ratios = parseRatios(jsonAst)
    val distributions = parseDistributions(jsonAst)

    Config(numSimulations,
           simulationSeconds,
           numTraders,
           parallel,
           logLevel,
           simRootPath,
           orderBookPath,
           ratios,
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
            case "pareto" =>
              val shape = coeffs("b")
              val ret = Pareto(scale, shape)
              scale = 1
              ret
            case "laplace" =>
              val ret = Laplace(loc, scale)
              loc = 0
              scale = 1
              ret
            case "gumbel_l" =>
              val ret = Gumbel(loc, scale)
              loc = 0
              scale = 1
              ret
          }

          new TransformedDistr(contDist, loc, scale)
      }
    }

  }

  def parseRatios(jsonAst: JsValue): Map[String, Double] = {
    val ratios = jsonAst.asJsObject
      .fields("ratios")
      .asJsObject
      .fields
      .map(kv => {
        val ratio = kv._2 match {
          case JsArray(Vector(JsNumber(a), JsNumber(b))) =>
            a.toDouble
        }
        (kv._1, ratio)
      })

    ratios
  }

  def getRatio(jsonAst: JsValue, name: String): Double = {
    jsonAst.asJsObject().fields(name) match {
      case JsArray(Vector(JsNumber(aRatio), JsNumber(_))) =>
        aRatio.toDouble
    }
  }

  def parseDistributions(jsonAst: JsValue): Map[String, TransformedDistr] = {
    import DistJsonProtocol._
    val distributions = jsonAst.asJsObject
      .fields("distributions")
      .asJsObject
      .fields
      .map(kv => (kv._1, kv._2.convertTo[TransformedDistr]))

    distributions
  }

}
