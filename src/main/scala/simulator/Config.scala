package simulator

import javafx.scene.paint.Color

import breeze.stats.distributions._
import spray.json._
import DefaultJsonProtocol._
import com.typesafe.scalalogging.Logger

import scala.io.Source

//import java.io.File
//case class Config(foo: Int = -1, out: File = new File("."), xyz: Boolean = false,
//                  libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
//                  mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
//                  jars: Seq[File] = Seq(), kwargs: Map[String,String] = Map())

case class Config(simRoot: String = "",
                  distributions: Map[String, ContinuousDistr[Double]] =
                    Map[String, ContinuousDistr[Double]]()) {

  object DistJsonProtocol extends DefaultJsonProtocol {

    private val logger = Logger(this.getClass)

    implicit object DistJsonFormat
        extends RootJsonFormat[ContinuousDistr[Double]] {
      def write(dist: ContinuousDistr[Double]) = {
        JsString("Not implemented")
      }

      def read(value: JsValue) = value match {
        case JsString(distString) =>
          logger.debug(distString)
          val distSplit = distString.split('(')
          val coeffsStr = distSplit(1).stripSuffix(")").split(", ")
          val coeffs: Map[String, Double] = coeffsStr.map(s => (s.split('=')(0), s.split('=')(1).toDouble)).toMap

          val loc = coeffs("loc")
          val scale = coeffs("scale")

          distSplit(0) match {
            case "expon" => {
              new Exponential(1 / scale)
            }
            case "lognorm" => {
              val stdDev = coeffs("s")
              new LogNormal(Math.log(scale), stdDev)
            }
            case "cauchy" => {
              new CauchyDistribution(loc, scale)
            }
            case "f" => {
              val numeratorDegFreedom = coeffs("dfn")
              val denominatorDegFreedom = coeffs("dfd")
              new FDistribution(numeratorDegFreedom, denominatorDegFreedom)
            }
            case "beta" => {
              val a = coeffs("a")
              val b = coeffs("b")
              new Beta(a, b)
            }
//            case "weibull_min" => {
//              new WeibullDistribution()
//            }
          }

      }
    }

  }

  def parseDistributions(
      distributionsPath: String): Map[String, ContinuousDistr[Double]] = {

    val json = Source.fromFile(distributionsPath).getLines.mkString("")
    val jsonAst = json.parseJson

    println("hello")

    import DistJsonProtocol._
    val distributions = jsonAst.asJsObject
      .fields("distributions")
      .asJsObject
      .fields
      .map(kv => (kv._1, kv._2.convertTo[ContinuousDistr[Double]]))
    println(distributions)
    //.convertTo[Map[String, ContinuousDistr[Double]]]

    distributions
  }
}
